package com.example.propertysupervision.protocol.codec;

import com.example.propertysupervision.protocol.model.DecodedField;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void shouldDecodeFieldsByOffset() {
        byte[] source = ("510021" + "24上海春冬物业管理有限公司").getBytes(GBK);

        DecodedField field1 = FieldCodec.decode(1, source, 0);

        assertEquals(1, field1.getFieldNo());
        assertEquals("10021", field1.getValue());
        assertEquals(6, field1.getNextOffset());

        DecodedField field48 = FieldCodec.decode(48, source, field1.getNextOffset());

        assertEquals(48, field48.getFieldNo());
        assertEquals("上海春冬物业管理有限公司", field48.getValue());
        assertEquals(source.length, field48.getNextOffset());
    }

    @Test
    void shouldRejectNonNumericLengthPrefix() {
        byte[] source = "A10021".getBytes(GBK);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> FieldCodec.decode(1, source, 0)
        );

        assertEquals("Field 1 的长度前缀包含非数字字符", exception.getMessage());
    }

    @Test
    void shouldRejectIncompleteFieldValue() {
        byte[] source = "5100".getBytes(GBK);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> FieldCodec.decode(1, source, 0)
        );

        assertEquals(
            "Field 1 数据不完整，声明长度=5，剩余字节=3",
            exception.getMessage()
        );
    }

}
