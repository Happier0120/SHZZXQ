package com.example.propertysupervision.protocol.inbound;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RetryingStoredInboundMessageProcessorTest {

    private final StoredInboundMessageProcessor processor =
            mock(StoredInboundMessageProcessor.class);

    private final RetryingStoredInboundMessageProcessor retryingProcessor =
            new RetryingStoredInboundMessageProcessor(processor);

    @Test
    void shouldProcessOnceWhenNoConflictOccurs() {
        retryingProcessor.process(42L);
        verify(processor).process(42L);
    }

    @Test
    void shouldRetryAfterConcurrentBusinessOrderCreation() {
        doThrow(conflict()).doNothing().when(processor).process(42L);

        retryingProcessor.process(42L);

        verify(processor, times(2)).process(42L);
    }

    @Test
    void shouldStopAfterThreeAttempts() {
        ConcurrentBusinessOrderCreationException exception = conflict();
        doThrow(exception).when(processor).process(42L);

        assertSame(exception, assertThrows(
                ConcurrentBusinessOrderCreationException.class,
                () -> retryingProcessor.process(42L)));
        verify(processor, times(3)).process(42L);
    }

    @Test
    void shouldNotRetryOtherDuplicateKeys() {
        DuplicateKeyException exception = new DuplicateKeyException("其他唯一键冲突");
        doThrow(exception).when(processor).process(42L);

        assertSame(exception, assertThrows(DuplicateKeyException.class,
                () -> retryingProcessor.process(42L)));
        verify(processor).process(42L);
    }

    @Test
    void shouldNotRetrySystemFailure() {
        IllegalStateException exception = new IllegalStateException("数据库故障");
        doThrow(exception).when(processor).process(42L);

        assertSame(exception, assertThrows(IllegalStateException.class,
                () -> retryingProcessor.process(42L)));
        verify(processor).process(42L);
    }

    private ConcurrentBusinessOrderCreationException conflict() {
        return new ConcurrentBusinessOrderCreationException(
                "260721000001910300", new DuplicateKeyException("并发创建业务单"));
    }
}
