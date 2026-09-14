package com.example.propertysupervision.protocol.inbound;

import com.example.propertysupervision.protocol.codec.MessageCodec;
import com.example.propertysupervision.protocol.codec.TechnicalAcknowledgementCodec;
import com.example.propertysupervision.protocol.model.ProtocolMessage;
import com.example.propertysupervision.protocol.model.ProtocolSegment;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InboundProtocolFrameHandlerTest {

    @Test
    void shouldProcessFrameAndReturnTechnicalAcknowledgement() throws Exception {

        byte[] requestFrame = createRequestFrame();

        AtomicReference<byte[]> processedFrame = new AtomicReference<byte[]>();

        InboundFrameProcessor processor = processedFrame::set;

        InboundProtocolFrameHandler handler = new InboundProtocolFrameHandler(processor);

        byte[] responseFrame = handler.handle(requestFrame);

        assertArrayEquals(
            requestFrame,
            processedFrame.get()
        );

        assertArrayEquals(
            TechnicalAcknowledgementCodec.encode(),
            responseFrame
        );
    }

    @Test
    void shouldNotReturnAcknowledgementWhenProcessingFails() {

        byte[] requestFrame = createRequestFrame();

        InboundFrameProcessor processor = frame -> {
            throw new IOException("模拟保存原始报文失败");
        };

        InboundProtocolFrameHandler handler = new InboundProtocolFrameHandler(processor);

        assertThrows(
            IOException.class,
            () -> handler.handle(requestFrame)
        );
    }

    private static byte[] createRequestFrame() {

        ProtocolSegment summary = new ProtocolSegment()
            .addField(1, "10021")
            .addField(3, "0");

        ProtocolMessage message = new ProtocolMessage("9103", summary);

        return MessageCodec.encode(message);
    }
}