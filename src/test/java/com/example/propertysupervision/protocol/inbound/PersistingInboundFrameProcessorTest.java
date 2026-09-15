package com.example.propertysupervision.protocol.inbound;

import com.example.propertysupervision.persistence.entity.SupMessage;
import com.example.propertysupervision.persistence.mapper.SupMessageMapper;
import com.example.propertysupervision.protocol.codec.MessageCodec;
import com.example.propertysupervision.protocol.model.ProtocolMessage;
import com.example.propertysupervision.protocol.model.ProtocolSegment;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PersistingInboundFrameProcessorTest {

    @Test
    void shouldSaveMessageAndDispatchAsyncParsing()
            throws Exception {

        SupMessageMapper mapper =
                mock(SupMessageMapper.class);

        InboundMessageDispatcher dispatcher =
                mock(InboundMessageDispatcher.class);

        /*
         * 模拟MyBatis插入成功并回填数据库主键。
         */
        when(mapper.insertSelective(
                any(SupMessage.class)
        )).thenAnswer(invocation -> {

            SupMessage message =
                    invocation.getArgument(0);

            message.setId(42L);

            return 1;
        });

        PersistingInboundFrameProcessor processor =
                new PersistingInboundFrameProcessor(
                        mapper,
                        dispatcher
                );

        byte[] requestFrame =
                createRequestFrame();

        processor.process(requestFrame);

        ArgumentCaptor<SupMessage> captor =
                ArgumentCaptor.forClass(
                        SupMessage.class
                );

        verify(mapper).insertSelective(
                captor.capture()
        );

        SupMessage saved = captor.getValue();

        assertEquals(
                42L,
                saved.getId()
        );

        assertEquals(
                "9103",
                saved.getTransactionCode()
        );

        assertEquals(
                "I",
                saved.getDirection()
        );

        assertEquals(
                "0",
                saved.getStatus()
        );

        assertArrayEquals(
                requestFrame,
                saved.getRawMessage()
        );

        assertNotSame(
                requestFrame,
                saved.getRawMessage()
        );

        /*
         * 确认落库成功后才提交异步任务。
         */
        verify(dispatcher).dispatch(42L);
    }

    @Test
    void shouldFailWhenDatabaseInsertFails() {

        SupMessageMapper mapper =
                mock(SupMessageMapper.class);

        InboundMessageDispatcher dispatcher =
                mock(InboundMessageDispatcher.class);

        RuntimeException databaseException =
                new RuntimeException(
                        "模拟数据库异常"
                );

        when(mapper.insertSelective(
                any(SupMessage.class)
        )).thenThrow(databaseException);

        PersistingInboundFrameProcessor processor =
                new PersistingInboundFrameProcessor(
                        mapper,
                        dispatcher
                );

        IOException exception =
                assertThrows(
                        IOException.class,
                        () -> processor.process(
                                createRequestFrame()
                        )
                );

        assertEquals(
                "保存入站原始报文失败，交易代码=9103",
                exception.getMessage()
        );

        assertEquals(
                databaseException,
                exception.getCause()
        );

        /*
         * 没有成功落库，不能提交异步解析。
         */
        verifyNoInteractions(dispatcher);
    }

    @Test
    void shouldFailWhenNoRowIsInserted() {

        SupMessageMapper mapper =
                mock(SupMessageMapper.class);

        InboundMessageDispatcher dispatcher =
                mock(InboundMessageDispatcher.class);

        when(mapper.insertSelective(
                any(SupMessage.class)
        )).thenReturn(0);

        PersistingInboundFrameProcessor processor =
                new PersistingInboundFrameProcessor(
                        mapper,
                        dispatcher
                );

        IOException exception =
                assertThrows(
                        IOException.class,
                        () -> processor.process(
                                createRequestFrame()
                        )
                );

        assertEquals(
                "保存入站原始报文失败，影响行数=0",
                exception.getMessage()
        );

        verifyNoInteractions(dispatcher);
    }

    @Test
    void shouldFailWhenGeneratedIdIsMissing() {

        SupMessageMapper mapper =
                mock(SupMessageMapper.class);

        InboundMessageDispatcher dispatcher =
                mock(InboundMessageDispatcher.class);

        /*
         * 插入返回1，但没有模拟主键回填。
         */
        when(mapper.insertSelective(
                any(SupMessage.class)
        )).thenReturn(1);

        PersistingInboundFrameProcessor processor =
                new PersistingInboundFrameProcessor(
                        mapper,
                        dispatcher
                );

        IOException exception =
                assertThrows(
                        IOException.class,
                        () -> processor.process(
                                createRequestFrame()
                        )
                );

        assertEquals(
                "保存入站原始报文后未返回主键ID",
                exception.getMessage()
        );

        verifyNoInteractions(dispatcher);
    }

    private static byte[] createRequestFrame() {

        ProtocolSegment summary =
                new ProtocolSegment()
                        .addField(1, "10021")
                        .addField(3, "0");

        ProtocolMessage message =
                new ProtocolMessage(
                        "9103",
                        summary
                );

        return MessageCodec.encode(message);
    }
}