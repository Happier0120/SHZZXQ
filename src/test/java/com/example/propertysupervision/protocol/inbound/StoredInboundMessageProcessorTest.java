package com.example.propertysupervision.protocol.inbound;

import com.example.propertysupervision.persistence.entity.SupMessage;
import com.example.propertysupervision.persistence.mapper.SupMessageMapper;
import com.example.propertysupervision.protocol.codec.MessageCodec;
import com.example.propertysupervision.protocol.model.ProtocolMessage;
import com.example.propertysupervision.protocol.model.ProtocolSegment;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class StoredInboundMessageProcessorTest {

    @Test
    void shouldParseAndUpdateStoredMessageFields() {

        SupMessageMapper mapper =
                mock(SupMessageMapper.class);

        SupMessage storedMessage =
                createPendingStoredMessage(
                        createComplete9103Request()
                );

        when(mapper.selectByPrimaryKey(42L))
                .thenReturn(storedMessage);

        when(mapper.updateParsedFields(
                42L,
                "260721000001910300",
                "10021",
                "11185"
        )).thenReturn(1);

        StoredInboundMessageProcessor processor =
                new StoredInboundMessageProcessor(mapper);

        processor.process(42L);

        verify(mapper).selectByPrimaryKey(42L);

        verify(mapper).updateParsedFields(
                42L,
                "260721000001910300",
                "10021",
                "11185"
        );

        verifyNoMoreInteractions(mapper);
    }

    @Test
    void shouldMarkMessageFailedWhenRawMessageIsInvalid() {

        SupMessageMapper mapper =
                mock(SupMessageMapper.class);

        SupMessage storedMessage =
                createPendingStoredMessage(
                        "invalid".getBytes(
                                StandardCharsets.US_ASCII
                        )
                );

        when(mapper.selectByPrimaryKey(42L))
                .thenReturn(storedMessage);

        when(mapper.markParseFailed(
                42L,
                "2",
                "报文解析失败：报文头不完整，至少需要11字节"
        )).thenReturn(1);

        StoredInboundMessageProcessor processor =
                new StoredInboundMessageProcessor(mapper);

        processor.process(42L);

        ArgumentCaptor<String> errorCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(mapper).selectByPrimaryKey(42L);

        verify(mapper).markParseFailed(
                eq(42L),
                eq("2"),
                errorCaptor.capture()
        );

        assertTrue(
                errorCaptor.getValue().contains(
                        "报文头不完整"
                )
        );

        verifyNoMoreInteractions(mapper);
    }

    @Test
    void shouldSkipMessageThatIsNotPending() {

        SupMessageMapper mapper =
                mock(SupMessageMapper.class);

        SupMessage storedMessage =
                createPendingStoredMessage(
                        createComplete9103Request()
                );

        /*
         * status=1表示这条报文已经完成技术处理。
         */
        storedMessage.setStatus("1");

        when(mapper.selectByPrimaryKey(42L))
                .thenReturn(storedMessage);

        StoredInboundMessageProcessor processor =
                new StoredInboundMessageProcessor(mapper);

        processor.process(42L);

        verify(mapper).selectByPrimaryKey(42L);

        /*
         * 除查询外，不应该再执行任何更新。
         */
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void shouldFailWhenStoredMessageDoesNotExist() {

        SupMessageMapper mapper =
                mock(SupMessageMapper.class);

        when(mapper.selectByPrimaryKey(42L))
                .thenReturn(null);

        StoredInboundMessageProcessor processor =
                new StoredInboundMessageProcessor(mapper);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> processor.process(42L)
                );

        assertEquals(
                "未找到已落库报文，messageId = 42",
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenParsedFieldsAreNotUpdated() {

        SupMessageMapper mapper =
                mock(SupMessageMapper.class);

        SupMessage storedMessage =
                createPendingStoredMessage(
                        createComplete9103Request()
                );

        when(mapper.selectByPrimaryKey(42L))
                .thenReturn(storedMessage);

        when(mapper.updateParsedFields(
                42L,
                "260721000001910300",
                "10021",
                "11185"
        )).thenReturn(0);

        StoredInboundMessageProcessor processor =
                new StoredInboundMessageProcessor(mapper);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> processor.process(42L)
                );

        assertEquals(
                "更新报文解析字段失败，"
                        + "messageId=42，影响行数=0",
                exception.getMessage()
        );
    }

    private static SupMessage createPendingStoredMessage(
            byte[] rawMessage) {

        SupMessage message = new SupMessage();

        message.setId(42L);
        message.setTransactionCode("9103");
        message.setDirection("I");
        message.setStatus("0");
        message.setRawMessage(rawMessage);

        return message;
    }

    private static byte[] createComplete9103Request() {

        ProtocolSegment summary =
                new ProtocolSegment()
                        .addField(1, "10021")
                        .addField(
                                2,
                                "260721000001910300"
                        )
                        .addField(3, "1");

        ProtocolSegment child =
                new ProtocolSegment()
                        .addField(1, "11185");

        ProtocolMessage message =
                new ProtocolMessage(
                        "9103",
                        summary
                ).addChildSegment(child);

        return MessageCodec.encode(message);
    }
}