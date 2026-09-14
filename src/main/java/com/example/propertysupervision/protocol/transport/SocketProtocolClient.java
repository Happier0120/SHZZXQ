package com.example.propertysupervision.protocol.transport;

import com.example.propertysupervision.protocol.codec.MessageCodec;
import com.example.propertysupervision.protocol.model.DecodedMessage;
import com.example.propertysupervision.protocol.model.ProtocolMessage;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class SocketProtocolClient {

    private final String host;
    private final int port;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;

    public SocketProtocolClient(String host, int port, int connectTimeoutMillis, int readTimeoutMillis) {

        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Socket服务地址不能为空");
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("非法Socket端口：" + port);
        }

        if (connectTimeoutMillis <= 0) {
            throw new IllegalArgumentException("连接超时时间必须大于0");
        }

        if (readTimeoutMillis <= 0) {
            throw new IllegalArgumentException("读取超时时间必须大于0");
        }

        this.host = host;
        this.port = port;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public DecodedMessage exchange(ProtocolMessage request) throws IOException {

        if (request == null) {
            throw new IllegalArgumentException("请求报文不能为空");
        }

        byte[] requestFrame = MessageCodec.encode(request);

        try (Socket socket = new Socket()) {
            socket.connect(
                new InetSocketAddress(host, port),
                connectTimeoutMillis
            );

            socket.setSoTimeout(readTimeoutMillis);
            socket.setTcpNoDelay(true);

            OutputStream output = socket.getOutputStream();

            output.write(requestFrame);
            output.flush();

            byte[] responseFrame = ProtocolFrameReader.readFrame(socket.getInputStream());

            if (responseFrame == null) {
                throw new EOFException("对端未返回响应报文就关闭了连接");
            }

            return MessageCodec.decode(responseFrame, 0);
        }
    }
}