package com.example.propertysupervision.protocol.transport;

import com.example.propertysupervision.protocol.codec.MessageCodec;
import com.example.propertysupervision.protocol.model.DecodedMessage;
import com.example.propertysupervision.protocol.model.ProtocolMessage;
import com.example.propertysupervision.protocol.model.ProtocolSegment;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SocketProtocolClientTest {

    @Test
    void shouldSendRequestAndDecodeFragmentedResponse() throws Exception {

        ProtocolMessage request = createCancellationRequest();

        ProtocolMessage response = createCancellationResponse();

        byte[] expectedRequest = MessageCodec.encode(request);

        byte[] responseFrame = MessageCodec.encode(response);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        try (ServerSocket serverSocket = new ServerSocket(
            0,
            1,
            InetAddress.getLoopbackAddress())) {

            Future<byte[]> receivedRequest =
                executor.submit(() -> {

                    try (Socket socket = serverSocket.accept()) {

                        byte[] requestFrame = ProtocolFrameReader.readFrame(
                            socket.getInputStream()
                        );

                        OutputStream output = socket.getOutputStream();

                        /*
                         * 模拟银行端把响应拆成多个小块发送。
                         */
                        for (int offset = 0; offset < responseFrame.length; offset += 3) {

                            int count = Math.min(3, responseFrame.length - offset);

                            output.write(
                                responseFrame,
                                offset,
                                count
                            );

                            output.flush();
                        }

                        return requestFrame;
                    }
                });

            SocketProtocolClient client = new SocketProtocolClient(
                InetAddress
                    .getLoopbackAddress()
                    .getHostAddress(),
                serverSocket.getLocalPort(),
                2_000,
                2_000
            );

            DecodedMessage decodedResponse = client.exchange(request);

            assertArrayEquals(
                expectedRequest,
                receivedRequest.get(
                    5,
                    TimeUnit.SECONDS
                )
            );

            assertEquals(
                "7002",
                decodedResponse
                    .getMessage()
                    .getTransactionCode()
            );

            assertEquals(
                "00020",
                decodedResponse
                    .getMessage()
                    .getSummarySegment()
                    .getFields()
                    .get(1)
            );

            assertEquals(
                "00",
                decodedResponse
                    .getMessage()
                    .getSummarySegment()
                    .getFields()
                    .get(9)
            );

            assertEquals(
                "撤销成功",
                decodedResponse
                    .getMessage()
                    .getSummarySegment()
                    .getFields()
                    .get(10)
            );

            assertEquals(
                0,
                decodedResponse
                    .getMessage()
                    .getChildSegments()
                    .size()
            );
        } finally {
            executor.shutdownNow();
        }
    }

    private static ProtocolMessage createCancellationRequest() {

        ProtocolSegment summary = new ProtocolSegment()
            .addField(1, "00019")
            .addField(2, "260721000001700200")
            .addField(3, "1")
            .addField(4, "00")
            .addField(5, "20260721150000")
            .addField(14, "1")
            .addField(15, "3");

        ProtocolSegment child = new ProtocolSegment()
            .addField(1, "01065")
            .addField(7, "303")
            .addField(8, "XXX");

        return new ProtocolMessage("7002", summary).addChildSegment(child);
    }

    private static ProtocolMessage createCancellationResponse() {

        ProtocolSegment summary = new ProtocolSegment()
            .addField(1, "00020")
            .addField(2, "2607210000017002xx")
            .addField(3, "0")
            .addField(5, "20260721150000")
            .addField(9, "00")
            .addField(10, "撤销成功")
            .addField(14, "1")
            .addField(15, "3");

        return new ProtocolMessage("7002", summary);
    }
}