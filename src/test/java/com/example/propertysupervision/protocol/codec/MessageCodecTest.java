package com.example.propertysupervision.protocol.codec;

import com.example.propertysupervision.protocol.model.DecodedMessage;
import com.example.propertysupervision.protocol.model.ProtocolMessage;
import com.example.propertysupervision.protocol.model.ProtocolSegment;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageCodecTest {

    private static final Charset GBK = Charset.forName("GBK");

    @Test
    void shouldEncodeComplete9103Request() {
        ProtocolSegment summary = new ProtocolSegment()
            .addField(1, "10021")
            .addField(2, "260721000001910300")
            .addField(3, "1")
            .addField(4, "00")
            .addField(5, "20260721150000")
            .addField(6, "20260721")
            .addField(14, "1")
            .addField(15, "3")
            .addField(32, "320006392257")
            .addField(33, "40");

        ProtocolSegment child = new ProtocolSegment()
            .addField(1, "11185")
            .addField(7, "333")
            .addField(23, "100000000005")
            .addField(32, "350000000001")
            .addField(33, "92")
            .addField(40, "320006392257")
            .addField(47, "#")
            .addField(48, "上海春冬物业管理有限公司")
            .addField(58, "#")
            .addField(61, "91310115MA1HAY5C10")
            .addField(65, "315587-03004836374")
            .addField(105, "0");

        ProtocolMessage message = new ProtocolMessage("9103", summary).addChildSegment(child);

        byte[] encoded = MessageCodec.encode(message);
        String actual = new String(encoded, GBK);

        String expected =
            "0000270"
            + "9103"
            + "FC060001800000000000000000000001"
            + "510021"
            + "18260721000001910300"
            + "11"
            + "200"
            + "1420260721150000"
            + "820260721"
            + "11"
            + "13"
            + "12320006392257"
            + "240"
            + "82000201810300488000000000800000"
            + "511185"
            + "3333"
            + "12100000000005"
            + "12350000000001"
            + "292"
            + "12320006392257"
            + "01#"
            + "24上海春冬物业管理有限公司"
            + "01#"
            + "1891310115MA1HAY5C10"
            + "18315587-03004836374"
            + "10";

        assertEquals(expected, actual);

        // 7位长度头 + 4位交易代码 + 270字节报文体
        assertEquals(281, encoded.length);
    }

    @Test
    void shouldEncode9065ResponseWithoutChildSegment() {
        ProtocolSegment summary = new ProtocolSegment()
            .addField(1, "10024")
            .addField(2, "260902000027906531")
            .addField(3, "0")
            .addField(5, "20260715163030")
            .addField(9, "00")
            .addField(10, "交易成功")
            .addField(32, "350000000896")
            .addField(33, "92")
            .addField(108, "1300");

        ProtocolMessage message = new ProtocolMessage("9065", summary);

        byte[] encoded = MessageCodec.encode(message);
        String actual = new String(encoded, GBK);

        String expected =
            "0000113"
            + "9065"
            + "E8C00001800000000000000000100000"
            + "510024"
            + "18260902000027906531"
            + "10"
            + "1420260715163030"
            + "200"
            + "008交易成功"
            + "12350000000896"
            + "292"
            + "041300";

        assertEquals(expected, actual);

        // 7位长度头 + 4位交易代码 + 113字节报文体
        assertEquals(124, encoded.length);
    }

    @Test
    void shouldRejectMessageWhenChildCountDoesNotMatchField3() {
        ProtocolSegment summary = new ProtocolSegment()
            .addField(1, "10021")
            .addField(3, "1");

        ProtocolMessage message = new ProtocolMessage("9103", summary);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> MessageCodec.encode(message)
        );

        assertEquals(
            "子报文数量不一致：Field 3 = 1，实际数量 = 0",
            exception.getMessage()
        );
    }

    @Test
    void shouldRejectMessageWithoutField3() {
        ProtocolSegment summary = new ProtocolSegment()
            .addField(1, "10021");

        ProtocolMessage message = new ProtocolMessage("9103", summary);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> MessageCodec.encode(message)
        );

        assertEquals(
            "汇总段 Field 3 必须是1至6位非负整数",
            exception.getMessage()
        );
    }

    @Test
    void shouldDecodeComplete9103Request() {
        ProtocolSegment summary = new ProtocolSegment()
            .addField(1, "10021")
            .addField(2, "260721000001910300")
            .addField(3, "1")
            .addField(4, "00")
            .addField(5, "20260721150000")
            .addField(6, "20260721")
            .addField(14, "1")
            .addField(15, "3")
            .addField(32, "320006392257")
            .addField(33, "40");

        ProtocolSegment child = new ProtocolSegment()
            .addField(1, "11185")
            .addField(7, "333")
            .addField(23, "100000000005")
            .addField(32, "350000000001")
            .addField(33, "92")
            .addField(40, "320006392257")
            .addField(47, "#")
            .addField(48, "上海春冬物业管理有限公司")
            .addField(58, "#")
            .addField(61, "91310115MA1HAY5C10")
            .addField(65, "315587-03004836374")
            .addField(105, "0");

        ProtocolMessage original = new ProtocolMessage("9103", summary).addChildSegment(child);

        byte[] encoded = MessageCodec.encode(original);

        DecodedMessage decoded = MessageCodec.decode(encoded, 0);

        assertEquals(270, decoded.getBodyLength());
        assertEquals(281, decoded.getNextOffset());

        assertEquals(
            "9103",
            decoded.getMessage().getTransactionCode()
        );

        assertEquals(
            summary.getFields(),
            decoded.getMessage()
                .getSummarySegment()
                .getFields()
        );

        assertEquals(
            1,
            decoded.getMessage()
                .getChildSegments()
                .size()
        );

        assertEquals(
            child.getFields(),
            decoded.getMessage()
                .getChildSegments()
                .get(0)
                .getFields()
        );
    }

    @Test
    void shouldRejectInvalidBodyLength() {
        byte[] source = "0000A109103".getBytes(
                StandardCharsets.US_ASCII
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> MessageCodec.decode(source, 0)
        );

        assertEquals(
                "报文体长度必须是7位数字",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectIncompleteBody() {
        byte[] source = "00002709103".getBytes(StandardCharsets.US_ASCII);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> MessageCodec.decode(source, 0)
        );

        assertEquals(
            "报文体不完整，声明长度=270，剩余字节=0",
            exception.getMessage()
        );
    }
}
