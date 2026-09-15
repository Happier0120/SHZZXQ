package com.example.propertysupervision.protocol.inbound;

import com.example.propertysupervision.persistence.entity.SupMessage;
import com.example.propertysupervision.persistence.mapper.SupMessageMapper;
import com.example.propertysupervision.protocol.codec.MessageCodec;
import com.example.propertysupervision.protocol.model.ProtocolMessage;
import com.example.propertysupervision.protocol.model.ProtocolSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class StoredInboundMessageProcessorTest {

    private static final Long MESSAGE_ID = 42L;

    private static final String MESSAGE_NO =
            "260721000001910300";

    private SupMessageMapper mapper;
    private InboundTransactionRouter router;
    private StoredInboundMessageProcessor processor;

    @BeforeEach
    void setUp() {

        mapper = mock(SupMessageMapper.class);
        router = mock(InboundTransactionRouter.class);

        processor = new StoredInboundMessageProcessor(
                mapper,
                router
        );
    }

    @Test
    void shouldParseDispatchAndMarkMessageSucceeded() {

        stubValidPendingMessage();

        when(mapper.markProcessingSucceeded(MESSAGE_ID))
                .thenReturn(1);

        processor.process(MESSAGE_ID);

        ArgumentCaptor<ProtocolMessage> messageCaptor =
                ArgumentCaptor.forClass(
                        ProtocolMessage.class
                );

        /*
         * 必须先回填解析字段，再路由，
         * 最后才能标记报文处理成功。
         */
        InOrder order = inOrder(mapper, router);

        order.verify(mapper)
                .selectByPrimaryKey(MESSAGE_ID);

        order.verify(mapper)
                .updateParsedFields(
                        MESSAGE_ID,
                        MESSAGE_NO,
                        "10021",
                        "11185"
                );

        order.verify(router)
                .dispatch(
                        eq(MESSAGE_ID),
                        messageCaptor.capture()
                );

        order.verify(mapper)
                .markProcessingSucceeded(MESSAGE_ID);

        order.verifyNoMoreInteractions();

        ProtocolMessage decodedMessage =
                messageCaptor.getValue();

        assertEquals(
                "9103",
                decodedMessage.getTransactionCode()
        );

        assertEquals(
                MESSAGE_NO,
                decodedMessage
                        .getSummarySegment()
                        .getFields()
                        .get(2)
        );

        assertEquals(
                1,
                decodedMessage.getChildSegments().size()
        );

        assertEquals(
                "上海春冬物业管理有限公司",
                decodedMessage
                        .getChildSegments()
                        .get(0)
                        .getFields()
                        .get(48)
        );
    }

    @Test
    void shouldMarkMessageFailedWhenRawMessageIsInvalid() {

        SupMessage storedMessage =
                createStoredMessage(
                        "invalid".getBytes(
                                StandardCharsets.US_ASCII
                        )
                );

        when(mapper.selectByPrimaryKey(MESSAGE_ID))
                .thenReturn(storedMessage);

        when(mapper.markParseFailed(
                eq(MESSAGE_ID),
                eq("2"),
                anyString()
        )).thenReturn(1);

        processor.process(MESSAGE_ID);

        ArgumentCaptor<String> errorCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(mapper).selectByPrimaryKey(MESSAGE_ID);

        verify(mapper).markParseFailed(
                eq(MESSAGE_ID),
                eq("2"),
                errorCaptor.capture()
        );

        assertTrue(
                errorCaptor.getValue().contains(
                        "报文头不完整"
                )
        );

        verifyNoInteractions(router);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void shouldSkipMessageThatIsNotPending() {

        SupMessage storedMessage =
                createStoredMessage(
                        MessageCodec.encode(
                                create9103Message()
                        )
                );

        storedMessage.setStatus("1");

        when(mapper.selectByPrimaryKey(MESSAGE_ID))
                .thenReturn(storedMessage);

        processor.process(MESSAGE_ID);

        verify(mapper).selectByPrimaryKey(MESSAGE_ID);

        verifyNoMoreInteractions(mapper);
        verifyNoInteractions(router);
    }

    @Test
    void shouldFailWhenStoredMessageDoesNotExist() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> processor.process(MESSAGE_ID)
                );

        assertEquals(
                "未找到已落库报文，messageId = 42",
                exception.getMessage()
        );

        verify(mapper).selectByPrimaryKey(MESSAGE_ID);

        verifyNoMoreInteractions(mapper);
        verifyNoInteractions(router);
    }

    @Test
    void shouldNotDispatchWhenParsedFieldsAreNotUpdated() {

        stubValidPendingMessage();

        when(mapper.updateParsedFields(
                MESSAGE_ID,
                MESSAGE_NO,
                "10021",
                "11185"
        )).thenReturn(0);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> processor.process(MESSAGE_ID)
                );

        assertEquals(
                "更新报文解析字段失败，"
                        + "messageId=42，影响行数=0",
                exception.getMessage()
        );

        verifyNoInteractions(router);

        verify(mapper, never())
                .markProcessingSucceeded(MESSAGE_ID);
    }

    @Test
    void shouldMarkMessageFailedWhenRouterRejectsMessage() {

        stubValidPendingMessage();

        doThrow(
                new IllegalArgumentException(
                        "监管账户类型不合法"
                )
        ).when(router).dispatch(
                eq(MESSAGE_ID),
                any(ProtocolMessage.class)
        );

        when(mapper.markParseFailed(
                eq(MESSAGE_ID),
                eq("2"),
                anyString()
        )).thenReturn(1);

        processor.process(MESSAGE_ID);

        ArgumentCaptor<String> errorCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(mapper).markParseFailed(
                eq(MESSAGE_ID),
                eq("2"),
                errorCaptor.capture()
        );

        assertTrue(
                errorCaptor.getValue().contains(
                        "监管账户类型不合法"
                )
        );

        verify(mapper, never())
                .markProcessingSucceeded(MESSAGE_ID);
    }

    @Test
    void shouldPropagateSystemExceptionFromRouter() {

        stubValidPendingMessage();

        IllegalStateException failure =
                new IllegalStateException(
                        "保存开户申请失败"
                );

        doThrow(failure)
                .when(router)
                .dispatch(
                        eq(MESSAGE_ID),
                        any(ProtocolMessage.class)
                );

        IllegalStateException actual =
                assertThrows(
                        IllegalStateException.class,
                        () -> processor.process(MESSAGE_ID)
                );

        assertSame(failure, actual);

        verify(mapper, never())
                .markProcessingSucceeded(MESSAGE_ID);

        verify(mapper, never())
                .markParseFailed(
                        any(Long.class),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void shouldFailWhenSuccessStatusIsNotUpdated() {

        stubValidPendingMessage();

        when(mapper.markProcessingSucceeded(MESSAGE_ID))
                .thenReturn(0);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> processor.process(MESSAGE_ID)
                );

        assertEquals(
                "更新报文处理成功状态失败，"
                        + "messageId=42，影响行数=0",
                exception.getMessage()
        );

        verify(router).dispatch(
                eq(MESSAGE_ID),
                any(ProtocolMessage.class)
        );

        verify(mapper, never())
                .markParseFailed(
                        any(Long.class),
                        anyString(),
                        anyString()
                );
    }

    private void stubValidPendingMessage() {

        SupMessage storedMessage =
                createStoredMessage(
                        MessageCodec.encode(
                                create9103Message()
                        )
                );

        when(mapper.selectByPrimaryKey(MESSAGE_ID))
                .thenReturn(storedMessage);

        when(mapper.updateParsedFields(
                MESSAGE_ID,
                MESSAGE_NO,
                "10021",
                "11185"
        )).thenReturn(1);
    }

    private static SupMessage createStoredMessage(
            byte[] rawMessage) {

        SupMessage message = new SupMessage();

        message.setId(MESSAGE_ID);
        message.setTransactionCode("9103");
        message.setDirection("I");
        message.setStatus("0");
        message.setRawMessage(rawMessage);

        return message;
    }

    private static ProtocolMessage create9103Message() {

        ProtocolSegment summary =
                new ProtocolSegment()
                        .addField(1, "10021")
                        .addField(2, MESSAGE_NO)
                        .addField(3, "1")
                        .addField(4, "00")
                        .addField(5, "20260721150000")
                        .addField(6, "20260721")
                        .addField(14, "1")
                        .addField(15, "3")
                        .addField(32, "350000000001")
                        .addField(33, "92");

        ProtocolSegment child =
                new ProtocolSegment()
                        .addField(1, "11185")
                        .addField(7, "333")
                        .addField(23, "100000000005")
                        .addField(32, "350000000001")
                        .addField(33, "92")
                        .addField(40, "320006392257")
                        .addField(47, "#")
                        .addField(
                                48,
                                "上海春冬物业管理有限公司"
                        )
                        .addField(58, "#")
                        .addField(
                                61,
                                "91310115MA1HAY5C10"
                        )
                        .addField(
                                65,
                                "315587-03004836374"
                        )
                        .addField(105, "0");

        return new ProtocolMessage(
                "9103",
                summary
        ).addChildSegment(child);
    }
}