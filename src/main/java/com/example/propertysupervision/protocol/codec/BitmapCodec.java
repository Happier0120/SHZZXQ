package com.example.propertysupervision.protocol.codec;

import java.util.Collection;

public final class BitmapCodec {
    private BitmapCodec() {

    }

    /**
     * 根据字段编号生成128位Bitmap，
     * 最终返回32位十六进制字符串。
     *
     * @param fieldNos 当前报文实际包含的Field编号
     * @param hasNext  后面是否还有下一段子报文
     */
    public static String encode(Collection<Integer> fieldNos, boolean hasNext) {
        if (fieldNos == null) {
            throw new IllegalArgumentException("Field 编号集合不能为空");
        }

        boolean[] bits = new boolean[128];

        // 1 ~ 127 为普通Field
        for (Integer fieldNo : fieldNos) {
            if (fieldNo == null) {
                throw new IllegalArgumentException("Field 编号不能为空");
            }

            if (fieldNo < 1 || fieldNo > 127) {
                throw new IllegalArgumentException("非法 Field 编号：" + fieldNo);
            }

            /**
             * Field 1 对应 bits[0]
             * Field 2 对应 bits[1]
             * ...
             * Field 127 对应 bits[126]
             */
            bits[fieldNo - 1] = true;
        }

        /**
         * 第128位：
         *
         * true  = 后面还有下一段
         * false = 当前已经是最后一段
         */
        bits[127] = hasNext;

        StringBuilder hex = new StringBuilder(32);

        /**
         * 每4个bit转成1个十六进制字符。
         *
         * 128 bit / 4 = 32个十六进制字符。
         */
        for (int i = 0; i < 128; i += 4) {
            int value = 0;

            if (bits[i]) {
                value += 8;
            }

            if (bits[i + 1]) {
                value += 4;
            }

            if (bits[i + 2]) {
                value += 2;
            }

            if (bits[i + 3]) {
                value += 1;
            }

            hex.append(Integer.toHexString(value).toUpperCase());
        }

        return hex.toString();
    }
}
