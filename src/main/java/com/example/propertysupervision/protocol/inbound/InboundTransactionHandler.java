package com.example.propertysupervision.protocol.inbound;

import com.example.propertysupervision.protocol.model.ProtocolMessage;

/**
 * 一种入站交易代码对应一个处理器实现。
 */
public interface InboundTransactionHandler {

    /**
     * 返回当前处理器支持的4位交易代码。
     *
     * 例如：
     * 监管账户开户处理器返回9103。
     */
    String supportedTransactionCode();

    /**
     * 处理已经完成协议解码的入站报文。
     */
    void handle(
        Long messageId,
        ProtocolMessage message
    );
}