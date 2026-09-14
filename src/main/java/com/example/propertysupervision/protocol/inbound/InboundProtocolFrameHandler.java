package com.example.propertysupervision.protocol.inbound;

import com.example.propertysupervision.protocol.codec.TechnicalAcknowledgementCodec;
import com.example.propertysupervision.protocol.transport.ProtocolFrameHandler;

import java.io.IOException;
import java.util.Arrays;

public final class InboundProtocolFrameHandler implements ProtocolFrameHandler {

    private final InboundFrameProcessor frameProcessor;

    public InboundProtocolFrameHandler(InboundFrameProcessor frameProcessor) {

        if (frameProcessor == null) {
            throw new IllegalArgumentException(
                "InboundFrameProcessor不能为空"
            );
        }

        this.frameProcessor = frameProcessor;
    }

    @Override
    public byte[] handle(byte[] requestFrame) throws IOException {

        if (requestFrame == null || requestFrame.length == 0) {
            throw new IllegalArgumentException(
                "入站原始报文不能为空"
            );
        }

        /*
         * 使用副本，避免后续异步处理时，
         * 原始数组被其他代码修改。
         */
        byte[] frameCopy = Arrays.copyOf(
            requestFrame,
            requestFrame.length
        );

        /*
         * 处理器成功接纳报文之后，
         * 才向物业中心返回9999技术应答。
         */
        frameProcessor.process(frameCopy);

        return TechnicalAcknowledgementCodec.encode();
    }
}