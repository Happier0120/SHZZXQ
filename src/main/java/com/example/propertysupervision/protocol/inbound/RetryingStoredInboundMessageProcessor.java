package com.example.propertysupervision.protocol.inbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RetryingStoredInboundMessageProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            RetryingStoredInboundMessageProcessor.class);

    // 包含第一次处理，最多执行三次。
    private static final int MAX_ATTEMPTS = 3;

    private final StoredInboundMessageProcessor processor;

    public RetryingStoredInboundMessageProcessor(StoredInboundMessageProcessor processor) {
        if (processor == null) {
            throw new IllegalArgumentException("StoredInboundMessageProcessor不能为空");
        }
        this.processor = processor;
    }

    /*
     * 重试循环不参与事务。每次调用另一个Spring Bean的process方法，
     * 都开启独立事务；抛出异常返回这里时，上一次事务已经回滚。
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void process(Long messageId) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                processor.process(messageId);
                return;
            } catch (ConcurrentBusinessOrderCreationException exception) {
                if (attempt == MAX_ATTEMPTS) {
                    throw exception;
                }
                LOGGER.info("业务单创建发生并发冲突，重新处理入站报文，messageId={}，nextAttempt={}",
                        messageId, attempt + 1);
            }
        }
    }
}
