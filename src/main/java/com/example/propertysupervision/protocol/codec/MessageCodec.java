package com.example.propertysupervision.protocol.codec;

import com.example.propertysupervision.protocol.model.DecodedMessage;
import com.example.propertysupervision.protocol.model.DecodedSegment;
import com.example.propertysupervision.protocol.model.ProtocolMessage;
import com.example.propertysupervision.protocol.model.ProtocolSegment;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class MessageCodec {

    private static final int LENGTH_DIGITS = 7;
    private static final int MAX_BODY_LENGTH = 9_999_999;
    private static final int TRANSACTION_CODE_LENGTH = 4;
    private static final int HEADER_LENGTH = LENGTH_DIGITS + TRANSACTION_CODE_LENGTH;

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

    public static DecodedMessage decode(byte[] source, int offset) {
        if (source == null) {
            throw new IllegalArgumentException("待解码字节数组不能为空");
        }

        if (offset < 0 || offset > source.length) {
            throw new IllegalArgumentException("非法解码位置：" + offset);
        }

        if (source.length - offset < HEADER_LENGTH) {
            throw new IllegalArgumentException("报文头不完整，至少需要11字节");
        }

        int bodyLength = parseBodyLength(source, offset);

        if (bodyLength == 0) {
            throw new IllegalArgumentException("报文体不能为空");
        }

        int transactionCodeOffset = offset + LENGTH_DIGITS;

        String transactionCode = parseTransactionCode(source, transactionCodeOffset);

        int bodyOffset = offset + HEADER_LENGTH;
        int remainingLength = source.length - bodyOffset;

        if (remainingLength < bodyLength) {
            throw new IllegalArgumentException(
                "报文体不完整，声明长度="
                    + bodyLength
                    + "，剩余字节="
                    + remainingLength
            );
        }

        int bodyEndOffset = bodyOffset + bodyLength;

        /*
         * 只把当前报文体交给SegmentCodec，防止字段解析越过
         * 当前报文边界，误读到下一条报文。
         */
        byte[] body = Arrays.copyOfRange(
            source,
            bodyOffset,
            bodyEndOffset
        );

        DecodedSegment decodedSummary = SegmentCodec.decode(body, 0);

        ProtocolMessage message = new ProtocolMessage(transactionCode, decodedSummary.getSegment());

        int currentOffset = decodedSummary.getNextOffset();
        boolean hasNext = decodedSummary.isHasNext();

        while (hasNext) {
            if (currentOffset >= body.length) {
                throw new IllegalArgumentException("Bitmap标志表示存在下一段，但报文体已结束");
            }

            DecodedSegment decodedChild = SegmentCodec.decode(body, currentOffset);

            message.addChildSegment(decodedChild.getSegment());

            currentOffset = decodedChild.getNextOffset();
            hasNext = decodedChild.isHasNext();
        }

        if (currentOffset != body.length) {
            throw new IllegalArgumentException(
                    "报文体存在未解析数据，剩余字节="
                        + (body.length - currentOffset)
            );
        }

        validateChildCount(
            message.getSummarySegment(),
            message.getChildSegments().size()
        );

        return new DecodedMessage(
            message,
            bodyLength,
            bodyEndOffset
        );
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

    private static int parseBodyLength(byte[] source, int offset) {

        int bodyLength = 0;

        for (int i = 0; i < LENGTH_DIGITS; i++) {
            byte current = source[offset + i];

            if (current < '0' || current > '9') {
                throw new IllegalArgumentException("报文体长度必须是7位数字");
            }

            bodyLength = bodyLength * 10 + (current - '0');
        }

        return bodyLength;
    }

    private static String parseTransactionCode(byte[] source, int offset) {

        for (int i = 0; i < TRANSACTION_CODE_LENGTH; i++) {
            byte current = source[offset + i];

            if (current < '0' || current > '9') {
                throw new IllegalArgumentException("交易代码必须是4位数字");
            }
        }

        return new String(
            source,
            offset,
            TRANSACTION_CODE_LENGTH,
            StandardCharsets.US_ASCII
        );
    }


}
