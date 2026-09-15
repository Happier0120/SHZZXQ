package com.example.propertysupervision.protocol.inbound;

/**
 * 负责投递已落库报文的后续处理任务。
 */
public interface InboundMessageDispatcher {

    void dispatch(Long messageId);
}