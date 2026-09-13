package com.example.propertysupervision.protocol.schema;

/**
 * 描述一个Field的物理编码规则
 *
 * 例如：
 *
 * new FieldSpec(48, 2, 80)
 *
 * 表示：
 * Field编号：48
 * 长度前缀位数：2
 * 最大长度：80字节
 */
public final class FieldSpec {

    private final int fieldNo;
    private final int lengthDigits;
    private final int maxLength;

    public FieldSpec(
            int fieldNo,
            int lengthDigits,
            int maxLength) {
        this.fieldNo = fieldNo;
        this.lengthDigits = lengthDigits;
        this.maxLength = maxLength;
    }

    public int getFieldNo() {
        return fieldNo;
    }

    public int getLengthDigits() {
        return lengthDigits;
    }

    public int getMaxLength() {
        return maxLength;
    }

    @Override
    public String toString() {
        return "FieldSpec{" +
                "fieldNo=" + fieldNo +
                ", lengthDigits=" + lengthDigits +
                ", maxLength=" + maxLength +
                '}';
    }
}