package com.example.propertysupervision.protocol.inbound;

import com.example.propertysupervision.protocol.model.ProtocolMessage;
import com.example.propertysupervision.protocol.model.ProtocolSegment;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InboundTransactionRouterTest {

    @Test
    void shouldDispatch9103ToMatchingHandler() {

        InboundTransactionHandler handler =
                mock(InboundTransactionHandler.class);

        when(handler.supportedTransactionCode())
                .thenReturn("9103");

        InboundTransactionRouter router =
                new InboundTransactionRouter(
                        Collections.singletonList(
                                handler
                        )
                );

        ProtocolMessage message =
                createMessage("9103");

        router.dispatch(
                42L,
                message
        );

        verify(handler).handle(
                42L,
                message
        );
    }

    @Test
    void shouldRejectUnsupportedTransactionCode() {

        InboundTransactionRouter router =
                new InboundTransactionRouter(
                        Collections.emptyList()
                );

        ProtocolMessage message =
                createMessage("9060");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> router.dispatch(
                                42L,
                                message
                        )
                );

        assertEquals(
                "无法识别交易代码：9060",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectDuplicateTransactionHandlers() {

        InboundTransactionHandler firstHandler =
                mock(InboundTransactionHandler.class);

        InboundTransactionHandler secondHandler =
                mock(InboundTransactionHandler.class);

        when(firstHandler.supportedTransactionCode())
                .thenReturn("9103");

        when(secondHandler.supportedTransactionCode())
                .thenReturn("9103");

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> new InboundTransactionRouter(
                                Arrays.asList(
                                        firstHandler,
                                        secondHandler
                                )
                        )
                );

        assertEquals(
                "交易代码存在多个处理器：9103",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectInvalidHandlerTransactionCode() {

        InboundTransactionHandler handler =
                mock(InboundTransactionHandler.class);

        when(handler.supportedTransactionCode())
                .thenReturn("91A3");

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> new InboundTransactionRouter(
                                Collections.singletonList(
                                        handler
                                )
                        )
                );

        assertEquals(
                "交易处理器返回了非法交易代码：91A3",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNullMessageId() {

        InboundTransactionHandler handler =
                mock(InboundTransactionHandler.class);

        when(handler.supportedTransactionCode())
                .thenReturn("9103");

        InboundTransactionRouter router =
                new InboundTransactionRouter(
                        Collections.singletonList(
                                handler
                        )
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> router.dispatch(
                                null,
                                createMessage("9103")
                        )
                );

        assertEquals(
                "报文ID不能为空",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNullProtocolMessage() {

        InboundTransactionHandler handler =
                mock(InboundTransactionHandler.class);

        when(handler.supportedTransactionCode())
                .thenReturn("9103");

        InboundTransactionRouter router =
                new InboundTransactionRouter(
                        Collections.singletonList(
                                handler
                        )
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> router.dispatch(
                                42L,
                                null
                        )
                );

        assertEquals(
                "协议报文不能为空",
                exception.getMessage()
        );
    }

    private static ProtocolMessage createMessage(
            String transactionCode) {

        ProtocolSegment summary =
                new ProtocolSegment()
                        .addField(1, "10021")
                        .addField(3, "0");

        return new ProtocolMessage(
                transactionCode,
                summary
        );
    }
}