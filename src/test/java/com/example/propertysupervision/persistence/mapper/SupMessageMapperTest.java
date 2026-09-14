package com.example.propertysupervision.persistence.mapper;

import com.example.propertysupervision.persistence.entity.SupMessage;
import com.example.propertysupervision.protocol.codec.MessageCodec;
import com.example.propertysupervision.protocol.model.ProtocolMessage;
import com.example.propertysupervision.protocol.model.ProtocolSegment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class SupMessageMapperTest {

    @Autowired
    private SupMessageMapper supMessageMapper;

    @Test
    void shouldInsertAndSelectInboundMessage() {

        byte[] rawMessage = createRequestFrame();

        SupMessage message = new SupMessage();
        message.setTransactionCode("9103");
        message.setDirection("I");
        message.setSummaryType("10021");
        message.setRawMessage(rawMessage);

        int affectedRows =
                supMessageMapper.insertSelective(message);

        assertEquals(1, affectedRows);
        assertNotNull(message.getId());

        SupMessage saved =
                supMessageMapper.selectByPrimaryKey(message.getId());

        assertNotNull(saved);
        assertEquals(message.getId(), saved.getId());
        assertEquals("9103", saved.getTransactionCode());
        assertEquals("I", saved.getDirection());
        assertEquals("10021", saved.getSummaryType());

        /*
         * status没有在Java中赋值，
         * 应当使用数据库默认值0。
         */
        assertEquals("0", saved.getStatus());

        /*
         * 验证MEDIUMBLOB中的原始报文字节
         * 没有被转码或者修改。
         */
        assertArrayEquals(rawMessage, saved.getRawMessage());

        /*
         * 这些时间由数据库默认值生成。
         */
        assertNotNull(saved.getMessageTime());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    private static byte[] createRequestFrame() {

        ProtocolSegment summary = new ProtocolSegment()
                .addField(1, "10021")
                .addField(3, "0");

        ProtocolMessage message =
                new ProtocolMessage("9103", summary);

        return MessageCodec.encode(message);
    }
}