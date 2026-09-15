package com.example.propertysupervision.protocol.inbound;

import com.example.propertysupervision.persistence.entity.SupAccountChange;
import com.example.propertysupervision.persistence.entity.SupBusinessOrder;
import com.example.propertysupervision.persistence.mapper.SupAccountChangeMapper;
import com.example.propertysupervision.persistence.mapper.SupBusinessOrderMapper;
import com.example.propertysupervision.persistence.mapper.SupMessageMapper;
import com.example.propertysupervision.protocol.model.ProtocolMessage;
import com.example.propertysupervision.protocol.model.ProtocolSegment;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AccountOpening9103HandlerTest {

    private static final Long MESSAGE_ID = 42L;
    private static final Long BUSINESS_ORDER_ID = 100L;

    private static final String MESSAGE_NO =
            "260721000001910300";

    @Test
    void shouldRequestRetryForConcurrentMessageNoConflict() {
        DuplicateKeyException exception = new DuplicateKeyException("插入业务单失败",
                new SQLException("Duplicate entry '" + MESSAGE_NO
                        + "' for key 'sup_business_order.uk_sup_business_order_message_no'",
                        "23000", 1062));

        RuntimeException actual = handleWithInsertFailure(exception);

        assertEquals(ConcurrentBusinessOrderCreationException.class, actual.getClass());
        assertSame(exception, actual.getCause());
    }

    @Test
    void shouldNotTreatOtherUniqueConstraintsAsRepeatedRequest() {
        DuplicateKeyException exception = new DuplicateKeyException("其他唯一键冲突",
                new SQLException("Duplicate entry '1' for key 'PRIMARY'", "23000", 1062));

        assertSame(exception, handleWithInsertFailure(exception));
    }

    private RuntimeException handleWithInsertFailure(DuplicateKeyException exception) {
        SupBusinessOrderMapper orderMapper = mock(SupBusinessOrderMapper.class);
        SupAccountChangeMapper changeMapper = mock(SupAccountChangeMapper.class);
        SupMessageMapper messageMapper = mock(SupMessageMapper.class);
        when(orderMapper.insertSelective(any(SupBusinessOrder.class))).thenThrow(exception);
        AccountOpening9103Handler handler = createHandler(
                orderMapper, changeMapper, messageMapper, new ObjectMapper());

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> handler.handle(MESSAGE_ID, createComplete9103Message("92")));

        verifyNoInteractions(changeMapper, messageMapper);
        return actual;
    }

    @Test
    void shouldSupportTransactionCode9103() {

        AccountOpening9103Handler handler =
                createHandler(
                        mock(SupBusinessOrderMapper.class),
                        mock(SupAccountChangeMapper.class),
                        mock(SupMessageMapper.class),
                        new ObjectMapper()
                );

        assertEquals(
                "9103",
                handler.supportedTransactionCode()
        );
    }

    @Test
    void shouldCreateBusinessOrderAndAccountChange()
            throws Exception {

        SupBusinessOrderMapper businessOrderMapper =
                mock(SupBusinessOrderMapper.class);

        SupAccountChangeMapper accountChangeMapper =
                mock(SupAccountChangeMapper.class);

        SupMessageMapper messageMapper =
                mock(SupMessageMapper.class);

        ObjectMapper objectMapper =
                new ObjectMapper();

        when(
                businessOrderMapper
                        .selectByInitiatingMessageNo(
                                MESSAGE_NO
                        )
        ).thenReturn(null);

        /*
         * 模拟MyBatis自增主键回填。
         */
        when(
                businessOrderMapper.insertSelective(
                        any(SupBusinessOrder.class)
                )
        ).thenAnswer(invocation -> {

            SupBusinessOrder order =
                    invocation.getArgument(0);

            order.setId(BUSINESS_ORDER_ID);

            return 1;
        });

        when(
                accountChangeMapper.insertSelective(
                        any(SupAccountChange.class)
                )
        ).thenReturn(1);

        when(
                messageMapper.bindBusinessOrder(
                        MESSAGE_ID,
                        BUSINESS_ORDER_ID
                )
        ).thenReturn(1);

        AccountOpening9103Handler handler =
                createHandler(
                        businessOrderMapper,
                        accountChangeMapper,
                        messageMapper,
                        objectMapper
                );

        handler.handle(
                MESSAGE_ID,
                createComplete9103Message("92")
        );

        ArgumentCaptor<SupBusinessOrder> orderCaptor =
                ArgumentCaptor.forClass(
                        SupBusinessOrder.class
                );

        verify(businessOrderMapper)
                .insertSelective(
                        orderCaptor.capture()
                );

        SupBusinessOrder order =
                orderCaptor.getValue();

        assertAll(
                () -> assertEquals(
                        MESSAGE_NO,
                        order.getInitiatingMessageNo()
                ),
                () -> assertEquals(
                        "9103",
                        order.getTransactionCode()
                ),
                () -> assertEquals(
                        "350000000001:320006392257:92",
                        order.getBusinessKey()
                ),
                () -> assertEquals(
                        "0",
                        order.getInitiator()
                ),
                () -> assertEquals(
                        "0",
                        order.getStatus()
                )
        );

        ArgumentCaptor<SupAccountChange> changeCaptor =
                ArgumentCaptor.forClass(
                        SupAccountChange.class
                );

        verify(accountChangeMapper)
                .insertSelective(
                        changeCaptor.capture()
                );

        SupAccountChange change =
                changeCaptor.getValue();

        assertAll(
                () -> assertEquals(
                        BUSINESS_ORDER_ID,
                        change.getBusinessOrderId()
                ),
                () -> assertNull(
                        change.getAccountId()
                ),
                () -> assertEquals(
                        "350000000001",
                        change.getPlatformFundNo()
                ),
                () -> assertEquals(
                        "320006392257",
                        change.getCommunityNo()
                ),
                () -> assertEquals(
                        "92",
                        change.getAccountType()
                ),
                () -> assertNull(
                        change.getBeforeData()
                ),
                () -> assertNull(
                        change.getAfterData()
                )
        );

        Map<String, String> requestData =
                objectMapper.readValue(
                        change.getRequestData(),
                        new TypeReference<
                                Map<String, String>>() {
                        }
                );

        assertEquals(
                expectedRequestData(),
                requestData
        );

        verify(messageMapper)
                .bindBusinessOrder(
                        MESSAGE_ID,
                        BUSINESS_ORDER_ID
                );
    }

    @Test
    void shouldReuseBusinessOrderForRepeatedMessage()
            throws Exception {

        SupBusinessOrderMapper businessOrderMapper =
                mock(SupBusinessOrderMapper.class);

        SupAccountChangeMapper accountChangeMapper =
                mock(SupAccountChangeMapper.class);

        SupMessageMapper messageMapper =
                mock(SupMessageMapper.class);

        ObjectMapper objectMapper =
                new ObjectMapper();

        SupBusinessOrder existingOrder =
                new SupBusinessOrder();

        existingOrder.setId(BUSINESS_ORDER_ID);
        existingOrder.setTransactionCode("9103");
        existingOrder.setBusinessKey(
                "350000000001:320006392257:92"
        );

        SupAccountChange existingChange =
                new SupAccountChange();

        existingChange.setBusinessOrderId(
                BUSINESS_ORDER_ID
        );

        existingChange.setRequestData(
                objectMapper.writeValueAsString(
                        expectedRequestData()
                )
        );

        when(
                businessOrderMapper
                        .selectByInitiatingMessageNo(
                                MESSAGE_NO
                        )
        ).thenReturn(existingOrder);

        when(
                accountChangeMapper
                        .selectByBusinessOrderId(
                                BUSINESS_ORDER_ID
                        )
        ).thenReturn(existingChange);

        when(
                messageMapper.bindBusinessOrder(
                        MESSAGE_ID,
                        BUSINESS_ORDER_ID
                )
        ).thenReturn(1);

        AccountOpening9103Handler handler =
                createHandler(
                        businessOrderMapper,
                        accountChangeMapper,
                        messageMapper,
                        objectMapper
                );

        handler.handle(
                MESSAGE_ID,
                createComplete9103Message("92")
        );

        verify(businessOrderMapper, never())
                .insertSelective(
                        any(SupBusinessOrder.class)
                );

        verify(accountChangeMapper, never())
                .insertSelective(
                        any(SupAccountChange.class)
                );

        verify(messageMapper)
                .bindBusinessOrder(
                        MESSAGE_ID,
                        BUSINESS_ORDER_ID
                );
    }

    @Test
    void shouldRejectRepeatedMessageWithDifferentContent()
            throws Exception {

        SupBusinessOrderMapper businessOrderMapper =
                mock(SupBusinessOrderMapper.class);

        SupAccountChangeMapper accountChangeMapper =
                mock(SupAccountChangeMapper.class);

        SupMessageMapper messageMapper =
                mock(SupMessageMapper.class);

        ObjectMapper objectMapper =
                new ObjectMapper();

        SupBusinessOrder existingOrder =
                new SupBusinessOrder();

        existingOrder.setId(BUSINESS_ORDER_ID);
        existingOrder.setTransactionCode("9103");
        existingOrder.setBusinessKey(
                "350000000001:320006392257:92"
        );

        SupAccountChange existingChange =
                new SupAccountChange();

        existingChange.setBusinessOrderId(
                BUSINESS_ORDER_ID
        );

        /*
         * 模拟相同报文编号对应了不同申请内容。
         */
        existingChange.setRequestData("{}");

        when(
                businessOrderMapper
                        .selectByInitiatingMessageNo(
                                MESSAGE_NO
                        )
        ).thenReturn(existingOrder);

        when(
                accountChangeMapper
                        .selectByBusinessOrderId(
                                BUSINESS_ORDER_ID
                        )
        ).thenReturn(existingChange);

        AccountOpening9103Handler handler =
                createHandler(
                        businessOrderMapper,
                        accountChangeMapper,
                        messageMapper,
                        objectMapper
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> handler.handle(
                                MESSAGE_ID,
                                createComplete9103Message("92")
                        )
                );

        assertEquals(
                "同一报文编号对应的开户申请内容不一致",
                exception.getMessage()
        );

        verify(messageMapper, never())
                .bindBusinessOrder(
                        any(Long.class),
                        any(Long.class)
                );
    }

    @Test
    void shouldRejectUnsupportedAccountType() {

        SupBusinessOrderMapper businessOrderMapper =
                mock(SupBusinessOrderMapper.class);

        SupAccountChangeMapper accountChangeMapper =
                mock(SupAccountChangeMapper.class);

        SupMessageMapper messageMapper =
                mock(SupMessageMapper.class);

        AccountOpening9103Handler handler =
                createHandler(
                        businessOrderMapper,
                        accountChangeMapper,
                        messageMapper,
                        new ObjectMapper()
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> handler.handle(
                                MESSAGE_ID,
                                createComplete9103Message("40")
                        )
                );

        assertEquals(
                "监管账户类型只允许91或92，实际为40",
                exception.getMessage()
        );

        /*
         * 字段校验在任何数据库操作之前完成。
         */
        verifyNoInteractions(
                businessOrderMapper,
                accountChangeMapper,
                messageMapper
        );
    }

    @Test
    void shouldRejectDifferentPlatformFundNoBetweenSegments() {

        SupBusinessOrderMapper businessOrderMapper =
                mock(SupBusinessOrderMapper.class);

        SupAccountChangeMapper accountChangeMapper =
                mock(SupAccountChangeMapper.class);

        SupMessageMapper messageMapper =
                mock(SupMessageMapper.class);

        AccountOpening9103Handler handler =
                createHandler(
                        businessOrderMapper,
                        accountChangeMapper,
                        messageMapper,
                        new ObjectMapper()
                );

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
                        .addField(32, "320006392257")
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

        ProtocolMessage message =
                new ProtocolMessage(
                        "9103",
                        summary
                ).addChildSegment(child);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> handler.handle(
                                MESSAGE_ID,
                                message
                        )
                );

        assertEquals(
                "维修资金平台标识在汇总报文和子报文中不一致",
                exception.getMessage()
        );

        verifyNoInteractions(
                businessOrderMapper,
                accountChangeMapper,
                messageMapper
        );
    }

    private static AccountOpening9103Handler createHandler(
            SupBusinessOrderMapper businessOrderMapper,
            SupAccountChangeMapper accountChangeMapper,
            SupMessageMapper messageMapper,
            ObjectMapper objectMapper) {

        return new AccountOpening9103Handler(
                businessOrderMapper,
                accountChangeMapper,
                messageMapper,
                objectMapper
        );
    }

    private static ProtocolMessage createComplete9103Message(
            String childAccountType) {

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
                        .addField(33, childAccountType);

        ProtocolSegment child =
                new ProtocolSegment()
                        .addField(1, "11185")
                        .addField(7, "333")
                        .addField(23, "100000000005")
                        .addField(32, "350000000001")
                        .addField(33, childAccountType)
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

    private static Map<String, String>
    expectedRequestData() {

        Map<String, String> values =
                new LinkedHashMap<String, String>();

        values.put("actionCode", "333");
        values.put(
                "platformFundNo",
                "350000000001"
        );
        values.put("accountType", "92");
        values.put(
                "communityNo",
                "320006392257"
        );
        values.put(
                "mixedCommunityFlag",
                "0"
        );
        values.put(
                "communityAccountNo1",
                "315587-03004836374"
        );
        values.put(
                "communityAccountBankNo1",
                "100000000005"
        );
        values.put(
                "communityAccountNo2",
                "#"
        );
        values.put(
                "communityAccountBankNo2",
                "#"
        );
        values.put(
                "propertyCompanyName",
                "上海春冬物业管理有限公司"
        );
        values.put(
                "propertyCreditCode",
                "91310115MA1HAY5C10"
        );

        return values;
    }
}
