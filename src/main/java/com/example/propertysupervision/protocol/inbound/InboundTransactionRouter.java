package com.example.propertysupervision.protocol.inbound;

import com.example.propertysupervision.protocol.model.ProtocolMessage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 根据交易代码选择对应的入站交易处理器。
 */
@Component
public class InboundTransactionRouter {

    private final Map<String, InboundTransactionHandler> handlers;

    public InboundTransactionRouter(List<InboundTransactionHandler> handlerList) {

        if (handlerList == null) {
            throw new IllegalArgumentException(
                "交易处理器列表不能为空"
            );
        }

        Map<String, InboundTransactionHandler> handlerMap = new HashMap<String, InboundTransactionHandler>();

        for (InboundTransactionHandler handler : handlerList) {

            if (handler == null) {
                throw new IllegalStateException(
                    "交易处理器不能为空"
                );
            }

            String transactionCode = handler.supportedTransactionCode();

            validateTransactionCode(transactionCode);

            InboundTransactionHandler previous = handlerMap.put(transactionCode, handler);

            /**
             * 每个交易代码只能有一个处理器。
             */
            if (previous != null) {
                throw new IllegalStateException("交易代码存在多个处理器：" + transactionCode);
            }
        }

        /**
         * 创建完成后不允许外部修改处理器注册表。
         */
        handlers = Collections.unmodifiableMap(handlerMap);
    }

    public void dispatch(Long messageId, ProtocolMessage message) {

        if (messageId == null) {
            throw new IllegalArgumentException(
                "报文ID不能为空"
            );
        }

        if (message == null) {
            throw new IllegalArgumentException(
                "协议报文不能为空"
            );
        }

        String transactionCode = message.getTransactionCode();

        InboundTransactionHandler handler = handlers.get(transactionCode);

        if (handler == null) {
            throw new IllegalArgumentException(
                "无法识别交易代码：" + transactionCode
            );
        }

        handler.handle(messageId, message);
    }

    private static void validateTransactionCode(String transactionCode) {

        if (transactionCode == null || !transactionCode.matches("\\d{4}")) {
            throw new IllegalStateException(
                "交易处理器返回了非法交易代码：" + transactionCode
            );
        }

    }


}
