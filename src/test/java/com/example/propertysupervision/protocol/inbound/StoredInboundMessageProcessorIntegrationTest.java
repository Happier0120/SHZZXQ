package com.example.propertysupervision.protocol.inbound;

import com.example.propertysupervision.persistence.entity.SupAccountChange;
import com.example.propertysupervision.persistence.entity.SupBusinessOrder;
import com.example.propertysupervision.persistence.entity.SupMessage;
import com.example.propertysupervision.persistence.mapper.SupAccountChangeMapper;
import com.example.propertysupervision.persistence.mapper.SupBusinessOrderMapper;
import com.example.propertysupervision.persistence.mapper.SupMessageMapper;
import com.example.propertysupervision.protocol.codec.MessageCodec;
import com.example.propertysupervision.protocol.model.ProtocolMessage;
import com.example.propertysupervision.protocol.model.ProtocolSegment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

/*
 * 不添加@Transactional：让process自己的事务真实提交或回滚。
 * 需要可连接的MySQL，以及DB_PASSWORD环境变量。
 */
@SpringBootTest(properties = {
        "bank.socket.server.enabled=false"
})
class StoredInboundMessageProcessorIntegrationTest {

    @Autowired
    private StoredInboundMessageProcessor processor;

    @Autowired
    private RetryingStoredInboundMessageProcessor retryingProcessor;

    @Autowired
    private SupMessageMapper messageMapper;

    @SpyBean
    private SupBusinessOrderMapper businessOrderMapper;

    @Autowired
    private SupAccountChangeMapper accountChangeMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @SpyBean
    private InboundTransactionRouter router;

    private String messageNo;

    private final List<Long> messageIds = new ArrayList<Long>();

    @BeforeEach
    void setUp() {
        // 使用独立的报文编号，避免复用已有业务记录。
        do {
            messageNo = LocalDate.now().format(
                    DateTimeFormatter.ofPattern("yyMMdd")
            ) + String.format(
                    "%06d",
                    ThreadLocalRandom.current().nextInt(1_000_000)
            ) + "910300";
        } while (businessOrderMapper.selectByInitiatingMessageNo(messageNo) != null);
    }

    @AfterEach
    void cleanUp() {
        // 按外键依赖顺序，只删除本次测试创建的数据。
        for (Long messageId : messageIds) {
            jdbcTemplate.update(
                    "delete from sup_message where id = ?",
                    messageId
            );
        }

        if (messageNo == null) {
            return;
        }

        SupBusinessOrder order =
                businessOrderMapper.selectByInitiatingMessageNo(messageNo);

        if (order != null) {
            jdbcTemplate.update(
                    "delete from sup_account_change where business_order_id = ?",
                    order.getId()
            );
            jdbcTemplate.update(
                    "delete from sup_business_order where id = ?",
                    order.getId()
            );
        }
    }

    @Test
    void shouldPersistAccountOpeningRequest() {
        Long messageId = insertPendingMessage();

        processor.process(messageId);

        SupMessage savedMessage = messageMapper.selectByPrimaryKey(messageId);

        assertEquals("1", savedMessage.getStatus());
        assertEquals(messageNo, savedMessage.getMessageNo());
        assertEquals("10021", savedMessage.getSummaryType());
        assertEquals("11185", savedMessage.getChildType());
        assertNull(savedMessage.getErrorMessage());
        assertNotNull(savedMessage.getBusinessOrderId());

        SupBusinessOrder order = businessOrderMapper.selectByPrimaryKey(
                savedMessage.getBusinessOrderId()
        );

        assertNotNull(order);
        assertEquals(messageNo, order.getInitiatingMessageNo());
        assertEquals("9103", order.getTransactionCode());
        // 报文技术处理成功，实际开户业务仍然待处理。
        assertEquals("0", order.getStatus());

        SupAccountChange change =
                accountChangeMapper.selectByBusinessOrderId(order.getId());

        assertNotNull(change);
        assertEquals("350000000001", change.getPlatformFundNo());
        assertEquals("320006392257", change.getCommunityNo());
        assertEquals("92", change.getAccountType());
        assertNotNull(change.getRequestData());
        assertNull(change.getAccountId());
        assertNull(change.getAfterData());
    }

    @Test
    void shouldReuseOrderForRepeatedRequest() {
        Long firstMessageId = insertPendingMessage();
        processor.process(firstMessageId);

        Long secondMessageId = insertPendingMessage();
        processor.process(secondMessageId);

        SupMessage first = messageMapper.selectByPrimaryKey(firstMessageId);
        SupMessage second = messageMapper.selectByPrimaryKey(secondMessageId);

        assertEquals("1", first.getStatus());
        assertEquals("1", second.getStatus());
        assertEquals(first.getBusinessOrderId(), second.getBusinessOrderId());

        Integer orderCount = jdbcTemplate.queryForObject(
                "select count(*) from sup_business_order "
                        + "where initiating_message_no = ?",
                Integer.class,
                messageNo
        );
        assertEquals(Integer.valueOf(1), orderCount);

        Integer changeCount = jdbcTemplate.queryForObject(
                "select count(*) from sup_account_change "
                        + "where business_order_id = ?",
                Integer.class,
                first.getBusinessOrderId()
        );
        assertEquals(Integer.valueOf(1), changeCount);
    }

    @Test
    void shouldReuseOrderForConcurrentRepeatedRequests() throws Exception {
        Long firstMessageId = insertPendingMessage();
        Long secondMessageId = insertPendingMessage();
        CyclicBarrier bothQueriesCompleted = new CyclicBarrier(2);
        AtomicInteger queryCount = new AtomicInteger();

        // 强制两个真实事务都先查到“不存在”，再同时进入插入分支。
        doAnswer(invocation -> {
            Object order = invocation.callRealMethod();
            if (queryCount.incrementAndGet() <= 2) {
                assertNull(order);
                bothQueriesCompleted.await(10, TimeUnit.SECONDS);
            }
            return order;
        }).when(businessOrderMapper).selectByInitiatingMessageNo(messageNo);

        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            Future<?> firstTask = workers.submit(() -> retryingProcessor.process(firstMessageId));
            Future<?> secondTask = workers.submit(() -> retryingProcessor.process(secondMessageId));
            firstTask.get(15, TimeUnit.SECONDS);
            secondTask.get(15, TimeUnit.SECONDS);
        } finally {
            workers.shutdownNow();
            assertTrue(workers.awaitTermination(10, TimeUnit.SECONDS), "测试处理线程未结束");
        }

        // 第三次查询来自冲突事务回滚后的重试，确保测试确实走过冲突分支。
        assertEquals(3, queryCount.get());
        SupMessage first = messageMapper.selectByPrimaryKey(firstMessageId);
        SupMessage second = messageMapper.selectByPrimaryKey(secondMessageId);
        assertEquals("1", first.getStatus());
        assertEquals("1", second.getStatus());
        assertEquals(messageNo, first.getMessageNo());
        assertEquals(messageNo, second.getMessageNo());
        assertNull(first.getErrorMessage());
        assertNull(second.getErrorMessage());
        assertNotNull(first.getBusinessOrderId());
        assertEquals(first.getBusinessOrderId(), second.getBusinessOrderId());
        assertEquals("0", businessOrderMapper.selectByPrimaryKey(first.getBusinessOrderId()).getStatus());
        assertEquals(Integer.valueOf(1), jdbcTemplate.queryForObject(
                "select count(*) from sup_business_order where initiating_message_no = ?",
                Integer.class, messageNo));
        assertEquals(Integer.valueOf(1), jdbcTemplate.queryForObject(
                "select count(*) from sup_account_change where business_order_id = ?",
                Integer.class, first.getBusinessOrderId()));
    }

    @Test
    void shouldRollbackWhenSystemFailureOccursAfterDispatch() {
        Long messageId = insertPendingMessage();

        // 执行真实Handler，在写入业务数据和关联报文后模拟故障。
        doAnswer(invocation -> {
            invocation.callRealMethod();
            throw new IllegalStateException("模拟业务落库后的系统异常");
        }).when(router).dispatch(eq(messageId), any(ProtocolMessage.class));

        assertThrows(
                IllegalStateException.class,
                () -> processor.process(messageId)
        );

        SupMessage savedMessage = messageMapper.selectByPrimaryKey(messageId);

        // 原始报文在process调用前保存，应保留以便后续重试。
        assertEquals("0", savedMessage.getStatus());
        assertNull(savedMessage.getMessageNo());
        assertNull(savedMessage.getSummaryType());
        assertNull(savedMessage.getChildType());
        assertNull(savedMessage.getBusinessOrderId());
        assertNull(businessOrderMapper.selectByInitiatingMessageNo(messageNo));
    }

    private Long insertPendingMessage() {
        SupMessage message = new SupMessage();
        message.setTransactionCode("9103");
        message.setDirection("I");
        message.setStatus("0");
        message.setRawMessage(MessageCodec.encode(create9103Message()));

        assertEquals(1, messageMapper.insertSelective(message));
        assertNotNull(message.getId());

        messageIds.add(message.getId());
        return message.getId();
    }

    private ProtocolMessage create9103Message() {
        ProtocolSegment summary = new ProtocolSegment()
                .addField(1, "10021")
                .addField(2, messageNo)
                .addField(3, "1")
                .addField(4, "00")
                .addField(5, "20260721150000")
                .addField(6, "20260721")
                .addField(14, "1")
                .addField(15, "3")
                .addField(32, "350000000001")
                .addField(33, "92");

        ProtocolSegment child = new ProtocolSegment()
                .addField(1, "11185")
                .addField(7, "333")
                .addField(23, "100000000005")
                .addField(32, "350000000001")
                .addField(33, "92")
                .addField(40, "320006392257")
                .addField(47, "#")
                .addField(48, "上海春冬物业管理有限公司")
                .addField(58, "#")
                .addField(61, "91310115MA1HAY5C10")
                .addField(65, "315587-03004836374")
                .addField(105, "0");

        return new ProtocolMessage("9103", summary).addChildSegment(child);
    }
}
