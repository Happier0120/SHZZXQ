package com.example.propertysupervision.protocol.codec;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TechnicalAcknowledgementCodecTest {

    @Test
    void shouldEncodeTechnicalAcknowledgement() {

        byte[] encoded = TechnicalAcknowledgementCodec.encode();

        assertArrayEquals(
            "00000009999".getBytes(
                StandardCharsets.US_ASCII
            ),
            encoded
        );
    }

    @Test
    void shouldRecognizeTechnicalAcknowledgement() {

        assertTrue(
            TechnicalAcknowledgementCodec.matches(
                "00000009999".getBytes(
                    StandardCharsets.US_ASCII
                )
            )
        );

        assertFalse(
            TechnicalAcknowledgementCodec.matches(
                "00000009103".getBytes(
                    StandardCharsets.US_ASCII
                )
            )
        );

        assertFalse(
            TechnicalAcknowledgementCodec.matches(null)
        );
    }
}