package com.example.propertysupervision.protocol.inbound;

import com.example.propertysupervision.protocol.config.InboundMessageAsyncConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest(classes = {
        InboundMessageAsyncConfiguration.class,
        AsyncInboundMessageDispatcher.class,
        RetryingStoredInboundMessageProcessor.class
})
class AsyncInboundMessageDispatcherTest {

    @Autowired
    private InboundMessageDispatcher dispatcher;

    @MockBean
    private StoredInboundMessageProcessor messageProcessor;

    @Test
    void shouldProcessMessageOnAsyncThread()
            throws Exception {

        Thread callingThread =
                Thread.currentThread();

        AtomicReference<Thread> processingThread =
                new AtomicReference<Thread>();

        CountDownLatch processed =
                new CountDownLatch(1);

        doAnswer(invocation -> {

            processingThread.set(
                    Thread.currentThread()
            );

            processed.countDown();

            return null;
        }).when(messageProcessor).process(42L);

        dispatcher.dispatch(42L);

        assertTrue(
                processed.await(
                        2,
                        TimeUnit.SECONDS
                ),
                "异步任务没有在2秒内执行"
        );

        assertNotSame(
                callingThread,
                processingThread.get()
        );

        assertTrue(
                processingThread
                        .get()
                        .getName()
                        .startsWith(
                                "inbound-message-"
                        )
        );
    }
}
