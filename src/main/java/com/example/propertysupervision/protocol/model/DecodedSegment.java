package com.example.propertysupervision.protocol.model;

public final class DecodedSegment {
    private final ProtocolSegment segment;
    private final boolean hasNext;
    private final int nextOffset;

    public DecodedSegment(ProtocolSegment segment, boolean hasNext, int nextOffset) {
        this.segment = segment;
        this.hasNext = hasNext;
        this.nextOffset = nextOffset;
    }

    public ProtocolSegment getSegment() {
        return segment;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public int getNextOffset() {
        return nextOffset;
    }
}
