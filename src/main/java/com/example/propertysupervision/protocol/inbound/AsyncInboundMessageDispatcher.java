package com.example.propertysupervision.protocol.inbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AsyncInboundMessageDispatcher implements InboundMessageDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        AsyncInboundMessageDispatcher.class
    );

    private final StoredInboundMessageProcessor messageProcessor;

    public AsyncInboundMessageDispatcher(StoredInboundMessageProcessor messageProcessor) {

        if (messageProcessor == null) {
            throw new IllegalArgumentException(
                    "StoredInboundMessageProcessor不能为空"
            );
        }

        this.messageProcessor = messageProcessor;
    }

    @Override
    @Async("inboundMessageExecutor")
    public void dispatch(Long messageId) {

        try {
            messageProcessor.process(messageId);
        } catch (RuntimeException exception) {
            /*
             * 数据库异常等系统故障：
             * 保留status=0，等待后续补偿处理。
             */
            LOGGER.error(
                "异步处理入站报文失败，messageId={}",
                messageId,
                exception
            );
        }
    }


}
