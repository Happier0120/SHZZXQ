package com.example.propertysupervision.protocol.inbound;

import com.example.propertysupervision.persistence.entity.SupMessage;
import com.example.propertysupervision.persistence.mapper.SupMessageMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
public final class PersistingInboundFrameProcessor implements InboundFrameProcessor {

    private static final int TRANSACTION_CODE_OFFSET = 7;
    private static final int TRANSACTION_CODE_LENGTH = 4;
    private static final int MINIMUM_FRAME_LENGTH = 11;

    private static final String DIRECTION_INBOUND = "I";
    private static final String STATUS_PROCESSING = "0";

    private final SupMessageMapper supMessageMapper;
    private final InboundMessageDispatcher messageDispatcher;

    public PersistingInboundFrameProcessor(
        SupMessageMapper supMessageMapper,
        InboundMessageDispatcher messageDispatcher
    ) {

        if (supMessageMapper == null) {
            throw new IllegalArgumentException(
                "SupMessageMapper不能为空"
            );
        }

        if (messageDispatcher == null) {
            throw new IllegalArgumentException(
                "InboundMessageDispatcher不能为空"
            );
        }

        this.supMessageMapper = supMessageMapper;
        this.messageDispatcher = messageDispatcher;

    }

    @Override
    public void process(byte[] requestFrame) throws IOException {

        if (requestFrame == null || requestFrame.length < MINIMUM_FRAME_LENGTH) {

            throw new IOException(
                    "完整入站报文至少需要11字节"
            );
        }

        /*
         * 这里只读取固定位置的交易代码，
         * 不解析Bitmap和业务字段。
         *
         * 原始报文必须先保存，
         * 后续才能进行协议解析和业务处理。
         */
        String transactionCode = new String(
            requestFrame,
            TRANSACTION_CODE_OFFSET,
            TRANSACTION_CODE_LENGTH,
            StandardCharsets.US_ASCII
        );

        SupMessage message = new SupMessage();
        message.setTransactionCode(transactionCode);
        message.setDirection(DIRECTION_INBOUND);
        message.setStatus(STATUS_PROCESSING);

        /*
         * 再保存一份副本，避免调用方后续修改原数组。
         */
        message.setRawMessage(
            Arrays.copyOf(
                requestFrame,
                requestFrame.length
            )
        );

        int affectedRows;

        try {
            affectedRows = supMessageMapper.insertSelective(message);
        } catch (RuntimeException exception) {
            throw new IOException(
                "保存入站原始报文失败，交易代码="
                    + transactionCode,
                    exception
            );
        }

        if (affectedRows != 1) {
            throw new IOException(
                "保存入站原始报文失败，影响行数="
                    + affectedRows
            );
        }

        if (message.getId() == null) {
            throw new IOException(
                "保存入站原始报文后未返回主键ID"
            );
        }

        /*
         * 报文已经落库，可以返回9999。
         * 后面的完整解析由异步线程执行。
         */
        messageDispatcher.dispatch(
            message.getId()
        );

    }
}