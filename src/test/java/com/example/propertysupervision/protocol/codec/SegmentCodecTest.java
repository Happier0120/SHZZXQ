package com.example.propertysupervision.protocol.codec;

import com.example.propertysupervision.protocol.model.DecodedSegment;
import com.example.propertysupervision.protocol.model.ProtocolSegment;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class SegmentCodecTest {

    private static final Charset GBK = Charset.forName("GBK");

    @Test
    void shouldEncode9103Summary021() {
        ProtocolSegment segment = new ProtocolSegment();

        /*
         * 故意不完全按照Field顺序添加，
         * 用于验证TreeMap会自动排序。
         */
        segment.addField(33, "40");
        segment.addField(1, "10021");
        segment.addField(32, "320006392257");
        segment.addField(2, "260721000001910300");
        segment.addField(15, "3");
        segment.addField(3, "1");
        segment.addField(14, "1");
        segment.addField(4, "00");
        segment.addField(6, "20260721");
        segment.addField(5, "20260721150000");

        /*
         * 021后面还有185，
         * 所以hasNext = true。
         */
        byte[] encoded = SegmentCodec.encode(segment, true);
        String actual = new String(encoded, GBK);

        String expected =
            "FC060001800000000000000000000001"
                + "510021"
                + "18260721000001910300"
                + "11"
                + "200"
                + "1420260721150000"
                + "820260721"
                + "11"
                + "13"
                + "12320006392257"
                + "240";

        assertEquals(expected, actual);

        /*
         * Bitmap      = 32 bytes
         * Field数据区 = 77 bytes
         *
         * 总长度：
         * 32 + 77 = 109 bytes
         */
        assertEquals(109, encoded.length);
    }

    @Test
    void shouldEncode9103Child185() {

        ProtocolSegment segment = new ProtocolSegment();

        /*
         * 9103监管账户开户的185子报文
         */
        segment.addField(1, "11185");
        segment.addField(7, "333");
        segment.addField(23, "100000000005");
        segment.addField(32, "350000000001");
        segment.addField(33, "92");
        segment.addField(40, "320006392257");
        segment.addField(47, "#");
        segment.addField(48, "上海春冬物业管理有限公司");
        segment.addField(58, "#");
        segment.addField(61, "91310115MA1HAY5C10");
        segment.addField(65, "315587-03004836374");
        segment.addField(105, "0");

        /*
         * 185已经是最后一个子报文，
         * 后面没有其他Segment。
         *
         * 所以Bitmap第128位 = 0。
         */
        byte[] encoded = SegmentCodec.encode(segment, false);

        String actual = new String(encoded, GBK);

        String expected =
            "82000201810300488000000000800000"
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

        /*
         * Bitmap = 32 bytes
         * Field数据区 = 129 bytes
         *
         * 185总长度：
         *
         * 32 + 129 = 161 bytes
         */
        assertEquals(161, encoded.length);
    }

    @Test
    void shouldDecode9103SegmentsInSequence() {
        ProtocolSegment expectedSummary = new ProtocolSegment()
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

        ProtocolSegment expectedChild = new ProtocolSegment()
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

        byte[] summaryBytes = SegmentCodec.encode(
            expectedSummary,
            true
        );

        byte[] childBytes = SegmentCodec.encode(
            expectedChild,
            false
        );

        byte[] body = new byte[summaryBytes.length + childBytes.length];

        System.arraycopy(
            summaryBytes,
            0,
            body,
            0,
            summaryBytes.length
        );

        System.arraycopy(
            childBytes,
            0,
            body,
            summaryBytes.length,
            childBytes.length
        );

        DecodedSegment decodedSummary = SegmentCodec.decode(body, 0);

        assertEquals(
            expectedSummary.getFields(),
            decodedSummary.getSegment().getFields()
        );
        assertTrue(decodedSummary.isHasNext());
        assertEquals(109, decodedSummary.getNextOffset());

        DecodedSegment decodedChild = SegmentCodec.decode(
            body,
            decodedSummary.getNextOffset()
        );

        assertEquals(
            expectedChild.getFields(),
            decodedChild.getSegment().getFields()
        );
        assertFalse(decodedChild.isHasNext());
        assertEquals(270, decodedChild.getNextOffset());
    }

    @Test
    void shouldRejectIncompleteBitmap() {
        byte[] source = "FC06".getBytes(StandardCharsets.US_ASCII);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SegmentCodec.decode(source, 0)
        );

        assertEquals(
            "报文段Bitmap不完整",
            exception.getMessage()
        );
    }

}
