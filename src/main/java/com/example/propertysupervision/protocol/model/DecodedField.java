package com.example.propertysupervision.protocol.model;

public final class DecodedField {

    private final int fieldNo;
    private final String value;
    private final int nextOffset;

    public DecodedField(int fieldNo, String value, int nextOffset) {
        this.fieldNo = fieldNo;
        this.value = value;
        this.nextOffset = nextOffset;
    }

    public int getFieldNo() {
        return fieldNo;
    }

    public String getValue() {
        return value;
    }

    /**
     * 当前Field结束后，下一个Field开始的字节位置。
     */
    public int getNextOffset() {
        return nextOffset;
    }
}
