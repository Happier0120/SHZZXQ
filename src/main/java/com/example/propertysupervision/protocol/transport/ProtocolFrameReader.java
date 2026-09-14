package com.example.propertysupervision.protocol.transport;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public final class ProtocolFrameReader {

    private static final int LENGTH_PREFIX_LENGTH = 7;
    private static final int TRANSACTION_CODE_LENGTH = 4;

    private ProtocolFrameReader() {
    }

    /**
     * 从输入流中读取一条完整报文。
     *
     * 报文结构：
     * 7位报文体长度 + 4位交易代码 + 报文体
     *
     * @return 完整报文字节；如果读取任何数据前流已关闭，则返回null
     */
    public static byte[] readFrame(InputStream input)
            throws IOException {

        if (input == null) {
            throw new IllegalArgumentException("InputStream不能为空");
        }

        byte[] lengthPrefix = new byte[LENGTH_PREFIX_LENGTH];

        /*
         * 单独读取第一个字节，用于区分：
         *
         * 1. 尚未开始新报文，连接正常关闭：返回null
         * 2. 已经开始读取报文，但中途关闭：抛出EOFException
         */
        int firstByte = input.read();

        if (firstByte == -1) {
            return null;
        }

        lengthPrefix[0] = (byte) firstByte;

        readFully(
            input,
            lengthPrefix,
            1,
            LENGTH_PREFIX_LENGTH - 1,
            "报文长度头"
        );

        int bodyLength = parseBodyLength(lengthPrefix);

        int remainingLength = TRANSACTION_CODE_LENGTH + bodyLength;

        byte[] frame = new byte[LENGTH_PREFIX_LENGTH + remainingLength];

        System.arraycopy(
            lengthPrefix,
            0,
            frame,
            0,
            LENGTH_PREFIX_LENGTH
        );

        readFully(
            input,
            frame,
            LENGTH_PREFIX_LENGTH,
            remainingLength,
            "交易代码及报文体"
        );

        return frame;
    }

    private static int parseBodyLength(byte[] lengthPrefix) throws IOException {

        int bodyLength = 0;

        for (byte current : lengthPrefix) {
            if (current < '0' || current > '9') {
                throw new IOException("报文体长度必须是7位数字");
            }

            bodyLength = bodyLength * 10 + (current - '0');
        }

        return bodyLength;
    }

    /**
     * 循环读取，直到填满指定区域。
     *
     * InputStream.read()不保证一次返回全部请求的字节。
     */
    private static void readFully(
        InputStream input,
        byte[] target,
        int offset,
        int length,
        String partName) throws IOException {

        int totalRead = 0;

        while (totalRead < length) {
            int count = input.read(
                target,
                offset + totalRead,
                length - totalRead
            );

            if (count == -1) {
                throw new EOFException(
                    partName
                        + "不完整，期望读取"
                        + length
                        + "字节，实际读取"
                        + totalRead
                        + "字节"
                );
            }

            /*
             * 某些特殊InputStream可能暂时返回0。
             * 再读取一个字节，避免循环没有进展。
             */
            if (count == 0) {
                int singleByte = input.read();

                if (singleByte == -1) {
                    throw new EOFException(
                        partName
                            + "不完整，期望读取"
                            + length
                            + "字节，实际读取"
                            + totalRead
                            + "字节"
                    );
                }

                target[offset + totalRead] = (byte) singleByte;
                totalRead++;
                continue;
            }

            totalRead += count;
        }
    }
}