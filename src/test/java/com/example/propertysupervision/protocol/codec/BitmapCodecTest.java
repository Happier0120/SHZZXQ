package com.example.propertysupervision.protocol.codec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

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

}
