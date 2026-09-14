package com.example.propertysupervision.protocol.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class ProtocolSocketServer implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolSocketServer.class);

    private final InetAddress bindAddress;
    private final int port;
    private final int backlog;
    private final int readTimeoutMillis;
    private final ProtocolFrameHandler frameHandler;
    private final ExecutorService workerExecutor;

    private volatile boolean running;
    private volatile boolean closed;

    private ServerSocket serverSocket;
    private Thread acceptThread;

    public ProtocolSocketServer(
        InetAddress bindAddress,
        int port,
        int backlog,
        int readTimeoutMillis,
        int workerThreads,
        ProtocolFrameHandler frameHandler) {

        if (bindAddress == null) {
            throw new IllegalArgumentException("Socket监听地址不能为空");
        }

        /*
         * 端口0只用于测试，由操作系统自动分配空闲端口。
         */
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException(
                    "非法Socket监听端口：" + port
            );
        }

        if (backlog <= 0) {
            throw new IllegalArgumentException(
                "Socket等待连接队列长度必须大于0"
            );
        }

        if (readTimeoutMillis <= 0) {
            throw new IllegalArgumentException(
                "Socket读取超时时间必须大于0"
            );
        }

        if (workerThreads <= 0) {
            throw new IllegalArgumentException(
                "Socket工作线程数必须大于0"
            );
        }

        if (frameHandler == null) {
            throw new IllegalArgumentException(
                "ProtocolFrameHandler不能为空"
            );
        }

        this.bindAddress = bindAddress;
        this.port = port;
        this.backlog = backlog;
        this.readTimeoutMillis = readTimeoutMillis;
        this.frameHandler = frameHandler;

        this.workerExecutor = Executors.newFixedThreadPool(
            workerThreads,
            namedThreadFactory("protocol-socket-worker-")
        );
    }

    public synchronized void start() throws IOException {

        if (closed) {
            throw new IllegalStateException(
                "ProtocolSocketServer已经关闭"
            );
        }

        if (running) {
            throw new IllegalStateException(
                "ProtocolSocketServer已经启动"
            );
        }

        ServerSocket newServerSocket = new ServerSocket();

        try {
            newServerSocket.bind(
                new InetSocketAddress(bindAddress, port),
                backlog
            );
        } catch (IOException exception) {
            newServerSocket.close();
            throw exception;
        }

        serverSocket = newServerSocket;
        running = true;

        acceptThread = new Thread(
            this::acceptLoop,
            "protocol-socket-acceptor"
        );

        acceptThread.start();
    }

    private void acceptLoop() {

        try {
            while (running) {
                Socket socket = serverSocket.accept();

                socket.setSoTimeout(readTimeoutMillis);
                socket.setTcpNoDelay(true);

                workerExecutor.execute(
                    () -> handleConnection(socket)
                );
            }
        } catch (SocketException exception) {
            /*
             * close()会关闭ServerSocket，从而唤醒accept()。
             * 正常关闭时不记录错误。
             */
            if (running) {
                LOGGER.error(
                    "银行协议Socket监听异常",
                    exception
                );
            }
        } catch (IOException exception) {
            if (running) {
                LOGGER.error(
                    "银行协议Socket接收连接失败",
                    exception
                );
            }
        } finally {
            running = false;
        }
    }

    private void handleConnection(Socket socket) {

        try (Socket currentSocket = socket) {

            byte[] requestFrame = ProtocolFrameReader.readFrame(
                currentSocket.getInputStream()
            );

            if (requestFrame == null) {
                return;
            }

            byte[] responseFrame = frameHandler.handle(requestFrame);

            if (responseFrame == null) {
                return;
            }

            OutputStream output = currentSocket.getOutputStream();

            output.write(responseFrame);
            output.flush();

        } catch (Exception exception) {
            LOGGER.error(
                "银行协议Socket连接处理失败",
                exception
            );
        }
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * 测试传入端口0时，用它取得操作系统实际分配的端口。
     */
    public int getLocalPort() {

        ServerSocket currentServerSocket = serverSocket;

        if (currentServerSocket == null) {
            return -1;
        }

        return currentServerSocket.getLocalPort();
    }

    @Override
    public synchronized void close() {

        if (closed) {
            return;
        }

        closed = true;
        running = false;

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException exception) {
                LOGGER.warn(
                    "关闭银行协议ServerSocket失败",
                    exception
                );
            }
        }

        workerExecutor.shutdownNow();
    }

    private static ThreadFactory namedThreadFactory(String prefix) {

        AtomicInteger sequence = new AtomicInteger();

        return task -> {
            Thread thread = new Thread(
                task,
                prefix + sequence.incrementAndGet()
            );

            thread.setDaemon(false);
            return thread;
        };
    }
}