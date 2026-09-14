package com.example.propertysupervision.protocol.codec;

import com.example.propertysupervision.protocol.model.DecodedField;
import com.example.propertysupervision.protocol.schema.FieldRegistry;
import com.example.propertysupervision.protocol.schema.FieldSpec;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class FieldCodec {

    private static final Charset GBK = Charset.forName("GBK");

    private FieldCodec() {

    }

    /**
     * 将一个Field编码为：
     *
     * [长度前缀] + [字段内容]
     *
     * 例如：
     *
     * Field1 = 10021
     * -> 510021
     *
     * Field47 = #
     * -> 01#
     *
     * Field48 = 上海春冬物业管理有限公司
     * -> 24上海春冬物业管理有限公司
     */
    public static byte[] encode(int fieldNo, String value) {
        if (value == null) {
            throw new IllegalArgumentException("Field " + fieldNo + " 的值不能为 null");
        }

        // 1. 查询Field的协议规则
        FieldSpec spec = FieldRegistry.get(fieldNo);

        // 2. 按GBK编码得到真正要发送的字节
        byte[] valueBytes = value.getBytes(GBK);

        // 3. 字段的真实长度必须按照字节计算
        int actualLength = valueBytes.length;

        // 4. 检查是否超过协议定义的最大长度
        if (actualLength > spec.getMaxLength()) {
            throw new IllegalArgumentException(
                    "Field " + fieldNo
                            + " 长度超过最大限制，actualLength="
                            + actualLength
                            + ", maxLength="
                            + spec.getMaxLength()
            );
        }

        /**
         * 5. 检查lengthDigits本身能不能表示这个长度。
         *
         * 例如：
         *
         * lengthDigits = 1
         * 最多只能表达 0~9
         *
         * lengthDigits = 2
         * 最多只能表达 0~99
         *
         * lengthDigits = 3
         * 最多只能表达 0~999
         */
        int maxLengthByPrefix = (int) Math.pow(10, spec.getLengthDigits()) - 1;

        if (actualLength > maxLengthByPrefix) {
            throw new IllegalArgumentException(
                    "Field " + fieldNo
                            + " 的实际长度 "
                            + actualLength
                            + " 无法使用 "
                            + spec.getLengthDigits()
                            + " 位长度前缀表示"
            );
        }

        // 6. 生成长度前缀
        String lengthPrefix = String.format(
                Locale.ROOT,
                "%0" + spec.getLengthDigits() + "d",
                actualLength
        );

        byte[] prefixBytes = lengthPrefix.getBytes(StandardCharsets.US_ASCII);

        // 7. 创建最终byte数组
        byte[] result = new byte[prefixBytes.length + valueBytes.length];

        // 8. 写入长度前缀
        System.arraycopy(prefixBytes, 0, result, 0, prefixBytes.length);

        // 9. 写入字段内容
        System.arraycopy(valueBytes, 0, result, prefixBytes.length, valueBytes.length);

        return result;
    }

    public static DecodedField decode(int fieldNo, byte[] source, int offset) {
        if (source == null) {
            throw new IllegalArgumentException("待解码字节数组不能为空");
        }

        if (offset < 0 || offset > source.length) {
            throw new IllegalArgumentException("非法解码位置：" + offset);
        }

        FieldSpec spec = FieldRegistry.get(fieldNo);
        int lengthDigits = spec.getLengthDigits();

        if (source.length - offset < lengthDigits) {
            throw new IllegalArgumentException("Field " + fieldNo + " 的长度前缀不完整");
        }

        int valueLength = 0;

        /*
         * 长度前缀使用ASCII数字。
         * 例如“024”会被解析成24。
         */
        for (int i = 0; i < lengthDigits; i++) {
            byte current = source[offset + i];

            if (current < '0' || current > '9') {
                throw new IllegalArgumentException(
                    "Field " + fieldNo + " 的长度前缀包含非数字字符"
                );
            }

            valueLength = valueLength * 10 + (current - '0');
        }

        if (valueLength > spec.getMaxLength()) {
            throw new IllegalArgumentException(
                "Field " + fieldNo
                    + " 声明长度超过最大限制，declaredLength="
                    + valueLength
                    + "，maxLength="
                    + spec.getMaxLength()
            );
        }

        int valueOffset = offset + lengthDigits;
        int remainingLength = source.length - valueOffset;

        if (remainingLength < valueLength) {
            throw new IllegalArgumentException(
                "Field " + fieldNo
                    + " 数据不完整，声明长度="
                    + valueLength
                    + "，剩余字节="
                    + remainingLength
            );
        }

        String value = new String(source, valueOffset, valueLength, GBK);

        int nextOffset = valueOffset + valueLength;

        return new DecodedField(fieldNo, value, nextOffset);
    }

}
