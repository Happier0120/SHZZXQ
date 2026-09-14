package com.example.propertysupervision.protocol.inbound;

import java.io.IOException;

@FunctionalInterface
public interface InboundFrameProcessor {

    /**
     * 接纳一条银行收到的完整原始报文。
     *
     * 后续实现中需要先保存SUP_MESSAGE，
     * 再安排异步解析和业务处理。
     */
    void process(byte[] requestFrame) throws IOException;
}