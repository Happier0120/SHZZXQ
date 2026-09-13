package com.example.propertysupervision.protocol.codec;

import com.example.propertysupervision.protocol.model.ProtocolSegment;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class SegmentCodec {

    private SegmentCodec() {
    }

    /**
     * 编码一段报文：
     *
     * Bitmap
     * +
     * Field1
     * +
     * Field2
     * +
     * ...
     *
     * @param segment 当前汇总报文或子报文
     * @param hasNext 后面是否还有下一段子报文
     */
    public static byte[] encode(ProtocolSegment segment, boolean hasNext) {
        if (segment == null) {
            throw new IllegalArgumentException("ProtocolSegment 不能为空");
        }

        if (segment.isEmpty()) {
            throw new IllegalArgumentException("ProtocolSegment 不能没有 Field");
        }

        // 1. 根据当前有哪些Field生成128位Bitmap
        String bitmap = BitmapCodec.encode(segment.getFields().keySet(), hasNext);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // 2. 先写入32字节Bitmap，Bitmap只包含0~9、A~F，所以使用ASCII编码
        byte[] bitmapBytes = bitmap.getBytes(StandardCharsets.US_ASCII);

        output.write(bitmapBytes, 0, bitmapBytes.length);

        // 3. 按Field编号从小到大进行编码。因为ProtocolSegment内部使用TreeMap，所以这里遍历出来天然就是：1 -> 2 -> 3 -> ...
        for (Map.Entry<Integer, String> entry : segment.getFields().entrySet()) {
            int fieldNo = entry.getKey();
            String value = entry.getValue();

            /*
             * 交给FieldCodec处理：
             *
             * 例如：
             *
             * 10021
             * ->
             * 510021
             *
             * #
             * ->
             * 01#
             */
            byte[] fieldBytes = FieldCodec.encode(fieldNo, value);
            output.write(fieldBytes, 0, fieldBytes.length);
        }

        return output.toByteArray();
    }
}
