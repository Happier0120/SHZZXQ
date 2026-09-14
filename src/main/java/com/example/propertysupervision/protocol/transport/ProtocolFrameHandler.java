package com.example.propertysupervision.protocol.transport;

import java.io.IOException;

@FunctionalInterface
public interface ProtocolFrameHandler {

    /**
     * 处理一条完整的入站报文。
     *
     * @param requestFrame 包含7位长度、4位交易代码和报文体
     * @return 同一Socket连接中返回的响应字节；
     *         返回null表示关闭连接但不发送响应
     */
    byte[] handle(byte[] requestFrame) throws IOException;
}