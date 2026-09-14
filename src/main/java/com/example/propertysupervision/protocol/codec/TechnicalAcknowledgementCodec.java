package com.example.propertysupervision.protocol.codec;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class TechnicalAcknowledgementCodec {

    private static final byte[] ACKNOWLEDGEMENT_FRAME = "00000009999".getBytes(StandardCharsets.US_ASCII);

    private TechnicalAcknowledgementCodec() {
    }

    /**
     * 按当前联调约定生成9999技术应答：
     *
     * 0000000：报文体长度为0
     * 9999：技术应答交易代码
     */
    public static byte[] encode() {
        return Arrays.copyOf(
            ACKNOWLEDGEMENT_FRAME,
            ACKNOWLEDGEMENT_FRAME.length
        );
    }

    /**
     * 判断一条完整报文是否为9999技术应答。
     */
    public static boolean matches(byte[] frame) {
        return frame != null
            && Arrays.equals(
            ACKNOWLEDGEMENT_FRAME,
            frame
        );
    }
}