package com.example.propertysupervision.protocol.transport;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProtocolFrameReaderTest {

    @Test
    void shouldReadCompleteFrame() throws IOException {
        byte[] source = (
            "0000005"
                + "9103"
                + "HELLO"
        ).getBytes(StandardCharsets.US_ASCII);

        byte[] frame = ProtocolFrameReader.readFrame(
            new ByteArrayInputStream(source)
        );

        assertArrayEquals(source, frame);
    }

    @Test
    void shouldHandleFragmentedReads() throws IOException {
        byte[] source = (
            "0000010"
                + "9103"
                + "HELLOWORLD"
        ).getBytes(StandardCharsets.US_ASCII);

        ChunkedInputStream input = new ChunkedInputStream(source, 2);

        byte[] frame = ProtocolFrameReader.readFrame(input);

        assertArrayEquals(source, frame);
    }

    @Test
    void shouldReadTwoFramesFromSameStream() throws IOException {

        byte[] first = (
            "0000005"
                + "9103"
                + "HELLO"
        ).getBytes(StandardCharsets.US_ASCII);

        byte[] second = (
            "0000005"
                + "9065"
                + "WORLD"
        ).getBytes(StandardCharsets.US_ASCII);

        byte[] source = new byte[first.length + second.length];

        System.arraycopy(
            first,
            0,
            source,
            0,
            first.length
        );

        System.arraycopy(
            second,
            0,
            source,
            first.length,
            second.length
        );

        ByteArrayInputStream input = new ByteArrayInputStream(source);

        assertArrayEquals(
            first,
            ProtocolFrameReader.readFrame(input)
        );

        assertArrayEquals(
            second,
            ProtocolFrameReader.readFrame(input)
        );

        assertNull(ProtocolFrameReader.readFrame(input));
    }

    @Test
    void shouldReturnNullWhenStreamEndsBeforeNextFrame() throws IOException {

        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);

        assertNull(ProtocolFrameReader.readFrame(input));
    }

    @Test
    void shouldRejectIncompleteLengthPrefix() {
        byte[] source = "0000".getBytes(
            StandardCharsets.US_ASCII
        );

        EOFException exception = assertThrows(
            EOFException.class,
            () -> ProtocolFrameReader.readFrame(
                new ByteArrayInputStream(source)
            )
        );

        assertEquals(
            "报文长度头不完整，期望读取6字节，实际读取3字节",
            exception.getMessage()
        );
    }

    @Test
    void shouldRejectIncompleteBody() {
        byte[] source = (
            "0000005"
                + "9103"
                + "ABC"
        ).getBytes(StandardCharsets.US_ASCII);

        EOFException exception = assertThrows(
            EOFException.class,
            () -> ProtocolFrameReader.readFrame(
                new ByteArrayInputStream(source)
            )
        );

        assertEquals(
            "交易代码及报文体不完整，期望读取9字节，实际读取7字节",
            exception.getMessage()
        );
    }

    @Test
    void shouldRejectNonNumericLengthPrefix() {
        byte[] source = (
            "0000A05"
                + "9103"
                + "HELLO"
        ).getBytes(StandardCharsets.US_ASCII);

        IOException exception = assertThrows(
            IOException.class,
            () -> ProtocolFrameReader.readFrame(
                new ByteArrayInputStream(source)
            )
        );

        assertEquals(
            "报文体长度必须是7位数字",
            exception.getMessage()
        );
    }

    /**
     * 模拟TCP一次最多只能返回少量字节。
     */
    private static final class ChunkedInputStream extends ByteArrayInputStream {

        private final int maxChunkSize;

        private ChunkedInputStream(byte[] source, int maxChunkSize) {
            super(source);
            this.maxChunkSize = maxChunkSize;
        }

        @Override
        public synchronized int read(byte[] target, int offset, int length) {
            return super.read(
                target,
                offset,
                Math.min(length, maxChunkSize)
            );
        }
    }
}