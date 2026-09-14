package com.example.propertysupervision.protocol.transport;

import com.example.propertysupervision.protocol.codec.MessageCodec;
import com.example.propertysupervision.protocol.model.ProtocolMessage;
import com.example.propertysupervision.protocol.model.ProtocolSegment;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProtocolSocketServerTest {

    @Test
    void shouldReceiveFrameAndWriteHandlerResponse() throws Exception {

        ProtocolSegment summary = new ProtocolSegment()
            .addField(1, "10021")
            .addField(3, "0");

        ProtocolMessage request = new ProtocolMessage("9103", summary);

        byte[] expectedRequest = MessageCodec.encode(request);

        byte[] expectedResponse = "ACK".getBytes(StandardCharsets.US_ASCII);

        AtomicReference<byte[]> receivedRequest = new AtomicReference<byte[]>();

        ProtocolFrameHandler handler = requestFrame -> {
            receivedRequest.set(requestFrame);
            return expectedResponse;
        };

        try (ProtocolSocketServer server = new ProtocolSocketServer(
             InetAddress.getLoopbackAddress(),
             0,
             10,
             2_000,
             2,
             handler
        )) {

            server.start();

            assertTrue(server.isRunning());

            try (Socket socket = new Socket()) {

                socket.connect(
                    new InetSocketAddress(
                        InetAddress.getLoopbackAddress(),
                        server.getLocalPort()
                    ),
                    2_000
                );

                socket.setSoTimeout(2_000);

                socket.getOutputStream().write(expectedRequest);
                socket.getOutputStream().flush();

                byte[] actualResponse = new byte[expectedResponse.length];

                new DataInputStream(socket.getInputStream())
                    .readFully(actualResponse);

                assertArrayEquals(
                    expectedResponse,
                    actualResponse
                );
            }

            assertArrayEquals(
                expectedRequest,
                receivedRequest.get()
            );
        }
    }
}