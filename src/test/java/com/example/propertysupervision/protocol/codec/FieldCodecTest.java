package com.example.propertysupervision.protocol.codec;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldCodecTest {

    private static final Charset GBK = Charset.forName("GBK");

    @Test
    void shouldEncodeField1() {
        byte[] encoded = FieldCodec.encode(1, "10021");
        assertEquals("510021", new String(encoded, GBK));
    }

    @Test
    void shouldEncodeField2() {
        byte[] encoded = FieldCodec.encode(2, "260721000001910300");
        assertEquals("18260721000001910300", new String(encoded, GBK));
    }

    @Test
    void shouldEncodeField3() {
        byte[] encoded = FieldCodec.encode(3, "1");
        assertEquals("11", new String(encoded, GBK));
    }

    @Test
    void shouldEncodeField4() {
        byte[] encoded = FieldCodec.encode(4, "00");
        assertEquals("200", new String(encoded, GBK));
    }

    @Test
    void shouldEncodeField5() {
        byte[] encoded = FieldCodec.encode(5, "20260721150000");
        assertEquals("1420260721150000", new String(encoded, GBK));
    }

    @Test
    void shouldEncodeField6() {
        byte[] encoded = FieldCodec.encode(6, "20260721");
        assertEquals("820260721", new String(encoded, GBK));
    }

    @Test
    void shouldEncodeField14() {
        byte[] encoded = FieldCodec.encode(14, "1");
        assertEquals("11", new String(encoded, GBK));
    }

    @Test
    void shouldEncodeField15() {
        byte[] encoded = FieldCodec.encode(15, "3");
        assertEquals("13", new String(encoded, GBK));
    }

    @Test
    void shouldEncodeField32() {
        byte[] encoded = FieldCodec.encode(32, "320006392257");
        assertEquals("12320006392257", new String(encoded, GBK));
    }

    @Test
    void shouldEncodeField33() {
        byte[] encoded = FieldCodec.encode(33, "40");
        assertEquals("240", new String(encoded, GBK));
    }

}
