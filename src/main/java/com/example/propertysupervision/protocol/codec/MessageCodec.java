package com.example.propertysupervision.protocol.codec;

import com.example.propertysupervision.protocol.model.ProtocolMessage;
import com.example.propertysupervision.protocol.model.ProtocolSegment;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MessageCodec {

    private static final int LENGTH_DIGITS = 7;
    private static final int MAX_BODY_LENGTH = 9_999_999;

    private MessageCodec() {

    }

    /**
     * 完整报文：
     * 7位报文体长度 + 4位交易代码 + 汇总段 + 所有子报文段。
     *
     * 长度头只统计汇总段和子报文段的字节数。
     */
    public static byte[] encode(ProtocolMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("ProtocolMessage 不能为空");
        }

        ProtocolSegment summary = message.getSummarySegment();
        List<ProtocolSegment> children = new ArrayList<>(message.getChildSegments());

        validateChildCount(summary, children.size());

        ByteArrayOutputStream body = new ByteArrayOutputStream();

        // 存在子报文时，汇总段的第128位置为1
        appendSegment(body, summary, !children.isEmpty());

        for (int i = 0; i < children.size(); i++) {
            boolean hasNext = i < children.size() - 1;
            appendSegment(body, children.get(i), hasNext);
        }

        String lengthPrefix = String.format(
            Locale.ROOT,
            "%0" + LENGTH_DIGITS + "d",
            body.size()
        );

        byte[] header = (lengthPrefix + message.getTransactionCode()).getBytes(StandardCharsets.US_ASCII);

        byte[] bodyBytes = body.toByteArray();
        byte[] result = new byte[header.length + bodyBytes.length];

        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(bodyBytes, 0, result, header.length, bodyBytes.length);

        return result;
    }

    /**
     * Field3的最大内容长度为6字节，内容必须是非负整数，
     * 并且与实际子报文数量一致。
     */
    private static void validateChildCount(ProtocolSegment summary, int actualCount) {
        String value = summary.getFields().get(3);

        if (value == null || !value.matches("[0-9]{1,6}")) {
            throw new IllegalArgumentException("汇总段 Field 3 必须是1至6位非负整数");
        }

        int declaredCount = Integer.parseInt(value);

        if (declaredCount != actualCount) {
            throw new IllegalArgumentException("子报文数量不一致：Field 3 = "
                + declaredCount
                + "，实际数量 = "
                + actualCount
            );
        }
    }

    private static void appendSegment(ByteArrayOutputStream body, ProtocolSegment segment, boolean hasNext) {
        byte[] encoded = SegmentCodec.encode(segment, hasNext);

        if (encoded.length > MAX_BODY_LENGTH - body.size()) {
            throw new IllegalArgumentException(
                "报文体长度超过7位长度头允许的最大值：" + MAX_BODY_LENGTH
            );
        }

        body.write(encoded, 0, encoded.length);
    }

}
