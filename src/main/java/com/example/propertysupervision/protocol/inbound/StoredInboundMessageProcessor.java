package com.example.propertysupervision.protocol.inbound;

import com.example.propertysupervision.persistence.entity.SupMessage;
import com.example.propertysupervision.persistence.mapper.SupMessageMapper;
import com.example.propertysupervision.protocol.codec.MessageCodec;
import com.example.propertysupervision.protocol.model.DecodedMessage;
import com.example.propertysupervision.protocol.model.ProtocolMessage;
import com.example.propertysupervision.protocol.model.ProtocolSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class StoredInboundMessageProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoredInboundMessageProcessor.class);

    private static final String STATUS_PENDING = "0";
    private static final String STATUS_FAILED = "2";

    private static final int MAX_ERROR_MESSAGE_LENGTH = 512;

    private final SupMessageMapper supMessageMapper;
    private final InboundTransactionRouter transactionRouter;

    public StoredInboundMessageProcessor(
        SupMessageMapper supMessageMapper,
        InboundTransactionRouter transactionRouter) {

        if (supMessageMapper == null) {
            throw new IllegalArgumentException("SupMessageMapper不能为空");
        }

        if (transactionRouter == null) {
            throw new IllegalArgumentException("InboundTransactionRouter不能为空");
        }

        this.supMessageMapper = supMessageMapper;
        this.transactionRouter = transactionRouter;

    }

    @Transactional
    public void process(Long messageId) {
        if (messageId == null) {
            throw new IllegalArgumentException("报文ID不能为空");
        }

        SupMessage storedMessage = supMessageMapper.selectByPrimaryKey(messageId);

        if (storedMessage == null) {
            throw new IllegalArgumentException("未找到已落库报文，messageId = " + messageId);
        }

        /**
         * 已经成功或者失败的报文不再重复解析。
         */
        if (!STATUS_PENDING.equals(storedMessage.getStatus())) {
            LOGGER.info("跳过非待处理报文，messageId={}，status={}", messageId, storedMessage.getStatus());
            return;
        }

        try {
            ParsedFields parsedFields = parse(storedMessage);

            int affectedRows = supMessageMapper.updateParsedFields(
                messageId,
                parsedFields.getMessageNo(),
                parsedFields.getSummaryType(),
                parsedFields.getChildType()
            );

            assertOneRowUpdated(
                affectedRows,
                messageId,
                "更新报文解析字段"
            );


            transactionRouter.dispatch(
                messageId,
                parsedFields.getProtocolMessage()
            );

            int successRows = supMessageMapper.markProcessingSucceeded(messageId);

            assertOneRowUpdated(
                successRows,
                messageId,
                "更新报文处理成功状态"
            );

            LOGGER.info(
                "入站报文处理成功，messageId={}，transactionCode={}",
                messageId,
                storedMessage.getTransactionCode()
            );


        } catch (IllegalArgumentException exception) {
            markParseFailed(messageId, exception);
        }

    }

    private ParsedFields parse(SupMessage storedMessage) {
        byte[] rawMessage = storedMessage.getRawMessage();

        if (rawMessage == null || rawMessage.length == 0) {
            throw new IllegalArgumentException("原始报文不能为空");
        }

        DecodedMessage decodedMessage = MessageCodec.decode(rawMessage, 0);

        /**
         * 确保raw_message中只有一条完整报文
         */
        if (decodedMessage.getNextOffset() != rawMessage.length) {
            throw new IllegalArgumentException("原始报文存在未解析数据，剩余字节=" + (rawMessage.length - decodedMessage.getNextOffset()));
        }

        ProtocolMessage protocolMessage = decodedMessage.getMessage();

        /**
         * 落库时从报文头读取过一次交易代码。
         * 完整解码后再次检查，避免数据不一致。
         */
        if (!protocolMessage.getTransactionCode().equals(storedMessage.getTransactionCode())) {
            throw new IllegalArgumentException("交易代码不一致，落库值=" + storedMessage.getTransactionCode() + "，解析值=" + protocolMessage.getTransactionCode());
        }

        ProtocolSegment summary = protocolMessage.getSummarySegment();

        String summaryType = requireField(summary, 1, "汇总报文类型");
        String messageNo = requireField(summary, 2, "报文编号");

        String childType = null;
        List<ProtocolSegment> childSegments = protocolMessage.getChildSegments();

        if (!childSegments.isEmpty()) {
            childType = requireField(childSegments.get(0), 1, "子报文类型"
            );
        }

        return new ParsedFields(protocolMessage, messageNo, summaryType, childType);

    }

    private static String requireField(ProtocolSegment segment, int fieldNo, String fieldName) {
        String value = segment.getFields().get(fieldNo);

        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(
                fieldName
                    + " Field "
                    + fieldNo
                    + " 不能为空"
            );
        }

        return value;

    }

    private void markParseFailed(Long messageId, IllegalArgumentException exception) {
        String errorMessage = "报文解析失败：" + exception.getMessage();
        errorMessage = truncate(errorMessage, MAX_ERROR_MESSAGE_LENGTH);

        int affectedRows = supMessageMapper.markParseFailed(
            messageId,
            STATUS_FAILED,
            errorMessage
        );

        assertOneRowUpdated(
            affectedRows,
            messageId,
            "更新报文解析失败状态"
        );

        LOGGER.warn(
            "入站报文解析失败，messageId={}，原因={}",
            messageId,
            exception.getMessage()
        );

    }

    private static String truncate(String value, int maximumLength) {
        if (value.length() <= maximumLength) {
            return value;
        }

        return value.substring(0, maximumLength);
    }

    private static void assertOneRowUpdated(int affectedRows, Long messageId, String operation) {
        if (affectedRows != 1) {
            throw new IllegalStateException(
                operation
                    + "失败，messageId="
                    + messageId
                    + "，影响行数="
                    + affectedRows
            );
        }
    }

    private static final class ParsedFields {

        private final ProtocolMessage protocolMessage;
        private final String messageNo;
        private final String summaryType;
        private final String childType;

        private ParsedFields(ProtocolMessage protocolMessage, String messageNo, String summaryType, String childType) {
            this.protocolMessage = protocolMessage;
            this.messageNo = messageNo;
            this.summaryType = summaryType;
            this.childType = childType;
        }

        private ProtocolMessage getProtocolMessage() {
            return protocolMessage;
        }

        private String getMessageNo() {
            return messageNo;
        }

        private String getSummaryType() {
            return summaryType;
        }

        private String getChildType() {
            return childType;
        }
    }

}
