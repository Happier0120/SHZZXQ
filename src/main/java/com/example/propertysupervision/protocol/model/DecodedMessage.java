package com.example.propertysupervision.protocol.model;

public final class DecodedMessage {

    private final ProtocolMessage message;
    private final int bodyLength;
    private final int nextOffset;

    public DecodedMessage(ProtocolMessage message, int bodyLength, int nextOffset) {
        this.message = message;
        this.bodyLength = bodyLength;
        this.nextOffset = nextOffset;
    }

    public ProtocolMessage getMessage() {
        return message;
    }

    public int getBodyLength() {
        return bodyLength;
    }

    /**
     * 当前完整报文结束后，下一条报文开始的位置。
     */
    public int getNextOffset() {
        return nextOffset;
    }
}
