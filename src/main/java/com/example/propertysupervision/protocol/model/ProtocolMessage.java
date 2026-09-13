package com.example.propertysupervision.protocol.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 表示一条完整的通信报文
 *
 * 结构：
 *
 * 7位报文体长度
 * + 4位交易代码
 * + 1条汇总报文
 * + 0条多条子报文（当前项目所需至多1条子报文）
 */

public final class ProtocolMessage {
    private final String transactionCode;
    private final ProtocolSegment summarySegment;
    private final List<ProtocolSegment> childSegments = new ArrayList<>();

    public ProtocolMessage(String transactionCode, ProtocolSegment summarySegment) {
        if (transactionCode == null || !transactionCode.matches("\\d{4}")) {
            throw new IllegalArgumentException("交易代码必须是4位数字");
        }

        if (summarySegment == null) {
            throw new IllegalArgumentException("汇总报文不能为空");
        }

        if (summarySegment.isEmpty()) {
            throw new IllegalArgumentException("汇总报文不能没有Field");
        }

        this.transactionCode = transactionCode;
        this.summarySegment = summarySegment;
    }

    public ProtocolMessage addChildSegment(ProtocolSegment childSegment) {
        if (childSegment == null) {
            throw new IllegalArgumentException("子报文不能为空");
        }

        if (childSegment.isEmpty()) {
            throw new IllegalArgumentException("子报文不能没有Field");
        }

        childSegments.add(childSegment);
        return this;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public ProtocolSegment getSummarySegment() {
        return summarySegment;
    }

    public List<ProtocolSegment> getChildSegments() {
        return Collections.unmodifiableList(childSegments);
    }

}
