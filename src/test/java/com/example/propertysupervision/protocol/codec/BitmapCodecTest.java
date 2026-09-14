package com.example.propertysupervision.protocol.codec;

import com.example.propertysupervision.protocol.model.DecodedBitmap;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class BitmapCodecTest {

    @Test
    void shouldGenerate9103SummaryBitmap() {
        String bitmap = BitmapCodec.encode(Arrays.asList(1, 2, 3, 4, 5, 6, 14, 15, 32, 33), true);
        assertEquals("FC060001800000000000000000000001", bitmap);
    }

    @Test
    void shouldGenerate9103Child185Bitmap() {
        String bitmap = BitmapCodec.encode(Arrays.asList(1, 7, 32, 33, 40, 105, 65, 23, 58, 47, 48, 61), false);
        assertEquals("82000201810300488000000000800000", bitmap);
    }


    @Test
    void shouldDecode9103SummaryBitmap() {
        DecodedBitmap decoded = BitmapCodec.decode("FC060001800000000000000000000001");

        assertEquals(
            Arrays.asList(1, 2, 3, 4, 5, 6, 14, 15, 32, 33),
            decoded.getFieldNos()
        );

        assertTrue(decoded.hasNext());
    }

    @Test
    void shouldDecode9103ChildBitmap() {
        DecodedBitmap decoded = BitmapCodec.decode("82000201810300488000000000800000");

        assertEquals(
            Arrays.asList(
                1, 7, 23, 32, 33, 40,
                47, 48, 58, 61, 65, 105
            ),
            decoded.getFieldNos()
        );

        assertFalse(decoded.hasNext());
    }

    @Test
    void shouldRejectBitmapWithInvalidLength() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> BitmapCodec.decode("FC06")
        );

        assertEquals(
            "Bitmap必须是32位十六进制字符串",
            exception.getMessage()
        );
    }

    @Test
    void shouldRejectBitmapWithInvalidCharacter() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> BitmapCodec.decode("FC06000180000000000000000000000Z")
        );

        assertEquals(
            "Bitmap必须是32位十六进制字符串",
            exception.getMessage()
        );
    }

}
