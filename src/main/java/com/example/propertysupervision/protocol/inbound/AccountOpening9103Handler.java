package com.example.propertysupervision.protocol.inbound;

import com.example.propertysupervision.persistence.entity.SupAccountChange;
import com.example.propertysupervision.persistence.entity.SupBusinessOrder;
import com.example.propertysupervision.persistence.mapper.SupAccountChangeMapper;
import com.example.propertysupervision.persistence.mapper.SupBusinessOrderMapper;
import com.example.propertysupervision.persistence.mapper.SupMessageMapper;
import com.example.propertysupervision.protocol.model.ProtocolMessage;
import com.example.propertysupervision.protocol.model.ProtocolSegment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Component
public class AccountOpening9103Handler
        implements InboundTransactionHandler {

    private static final String TRANSACTION_CODE = "9103";
    private static final String SUMMARY_TYPE = "10021";
    private static final String CHILD_TYPE = "11185";
    private static final String ACTION_CODE = "333";

    private static final String INITIATOR_PROPERTY_CENTER = "0";
    private static final String BUSINESS_STATUS_PENDING = "0";

    private static final Set<Integer> SUMMARY_FIELDS =
            fieldNumbers(
                    1, 2, 3, 4, 5, 6, 14, 15, 32, 33
            );

    private static final Set<Integer> CHILD_FIELDS =
            fieldNumbers(
                    1, 7, 23, 32, 33, 40,
                    47, 48, 58, 61, 65, 105
            );

    private final SupBusinessOrderMapper businessOrderMapper;
    private final SupAccountChangeMapper accountChangeMapper;
    private final SupMessageMapper messageMapper;
    private final ObjectMapper objectMapper;

    public AccountOpening9103Handler(
            SupBusinessOrderMapper businessOrderMapper,
            SupAccountChangeMapper accountChangeMapper,
            SupMessageMapper messageMapper,
            ObjectMapper objectMapper) {

        if (businessOrderMapper == null) {
            throw new IllegalArgumentException(
                    "SupBusinessOrderMapper不能为空"
            );
        }

        if (accountChangeMapper == null) {
            throw new IllegalArgumentException(
                    "SupAccountChangeMapper不能为空"
            );
        }

        if (messageMapper == null) {
            throw new IllegalArgumentException(
                    "SupMessageMapper不能为空"
            );
        }

        if (objectMapper == null) {
            throw new IllegalArgumentException(
                    "ObjectMapper不能为空"
            );
        }

        this.businessOrderMapper = businessOrderMapper;
        this.accountChangeMapper = accountChangeMapper;
        this.messageMapper = messageMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public String supportedTransactionCode() {
        return TRANSACTION_CODE;
    }

    @Override
    @Transactional(
            propagation = Propagation.MANDATORY,
            noRollbackFor = IllegalArgumentException.class
    )
    public void handle(
            Long messageId,
            ProtocolMessage message) {

        if (messageId == null) {
            throw new IllegalArgumentException(
                    "报文ID不能为空"
            );
        }

        if (message == null) {
            throw new IllegalArgumentException(
                    "协议报文不能为空"
            );
        }

        if (!TRANSACTION_CODE.equals(
                message.getTransactionCode())) {

            throw new IllegalArgumentException(
                    "监管账户开户处理器不支持交易代码："
                            + message.getTransactionCode()
            );
        }

        AccountOpeningData request =
                parseAndValidate(message);

        String requestJson =
                serializeRequest(request);

        String businessKey =
                createBusinessKey(request);

        SupBusinessOrder existingOrder =
                businessOrderMapper
                        .selectByInitiatingMessageNo(
                                request.messageNo
                        );

        Long businessOrderId;

        if (existingOrder == null) {
            businessOrderId = createBusinessOrder(
                    request,
                    businessKey
            );

            createAccountChange(
                    businessOrderId,
                    request,
                    requestJson
            );
        } else {
            validateRepeatedRequest(
                    existingOrder,
                    businessKey,
                    requestJson
            );

            businessOrderId = existingOrder.getId();
        }

        int affectedRows =
                messageMapper.bindBusinessOrder(
                        messageId,
                        businessOrderId
                );

        assertOneRowUpdated(
                affectedRows,
                "关联报文与业务主单"
        );
    }

    private AccountOpeningData parseAndValidate(
            ProtocolMessage message) {

        ProtocolSegment summary =
                message.getSummarySegment();

        requireExactFields(
                summary,
                SUMMARY_FIELDS,
                "9103汇总报文"
        );

        requireValue(
                summary,
                1,
                SUMMARY_TYPE,
                "汇总报文类型"
        );

        requireValue(
                summary,
                3,
                "1",
                "子报文数"
        );

        requireValue(
                summary,
                4,
                "00",
                "报文发起方"
        );

        requireValue(
                summary,
                14,
                "1",
                "交易方式"
        );

        requireValue(
                summary,
                15,
                "3",
                "业务种类"
        );

        List<ProtocolSegment> children =
                message.getChildSegments();

        if (children.size() != 1) {
            throw new IllegalArgumentException(
                    "9103必须包含1条11185子报文，实际数量="
                            + children.size()
            );
        }

        ProtocolSegment child = children.get(0);

        requireExactFields(
                child,
                CHILD_FIELDS,
                "11185子报文"
        );

        requireValue(
                child,
                1,
                CHILD_TYPE,
                "子报文类型"
        );

        requireValue(
                child,
                7,
                ACTION_CODE,
                "动作代码"
        );

        String messageNo =
                requireField(summary, 2, "报文编号");

        String platformFundNo =
                requireField(child, 32, "维修资金平台标识");

        String accountType =
                requireField(child, 33, "监管账户类型");

        String communityNo =
                requireField(child, 40, "小区平台唯一编号");

        String mixedCommunityFlag =
                requireField(child, 105, "是否混合小区");

        requireOneOf(
                accountType,
                "监管账户类型",
                "91",
                "92"
        );

        requireOneOf(
                mixedCommunityFlag,
                "是否混合小区",
                "0",
                "1"
        );

        requireSameValue(
                summary,
                child,
                32,
                "维修资金平台标识"
        );

        requireSameValue(
                summary,
                child,
                33,
                "监管账户类型"
        );

        return new AccountOpeningData(
                messageNo,
                platformFundNo,
                accountType,
                communityNo,
                mixedCommunityFlag,
                requireField(child, 65, "小区会计账号1"),
                requireField(child, 23, "小区会计账号1行号"),
                requireField(child, 58, "小区会计账号2"),
                requireField(child, 47, "小区会计账号2行号"),
                requireField(child, 48, "物业企业名称"),
                requireField(child, 61, "物业企业统一社会信用代码")
        );
    }

    private Long createBusinessOrder(
            AccountOpeningData request,
            String businessKey) {

        SupBusinessOrder order =
                new SupBusinessOrder();

        order.setInitiatingMessageNo(
                request.messageNo
        );

        order.setTransactionCode(
                TRANSACTION_CODE
        );

        order.setBusinessKey(
                businessKey
        );

        order.setInitiator(
                INITIATOR_PROPERTY_CENTER
        );

        order.setStatus(
                BUSINESS_STATUS_PENDING
        );

        int affectedRows;
        try {
            affectedRows = businessOrderMapper.insertSelective(order);
        } catch (DuplicateKeyException exception) {
            if (isMessageNoConflict(exception)) {
                // 不在当前事务里继续查询：让异常穿过事务代理，先完整回滚。
                throw new ConcurrentBusinessOrderCreationException(request.messageNo, exception);
            }
            throw exception;
        }

        assertOneRowUpdated(
                affectedRows,
                "创建9103业务主单"
        );

        if (order.getId() == null) {
            throw new IllegalStateException(
                    "创建9103业务主单后未返回主键ID"
            );
        }

        return order.getId();
    }

    private boolean isMessageNoConflict(DuplicateKeyException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException) {
                SQLException sqlException = (SQLException) cause;
                String detail = sqlException.getMessage();
                // MySQL 1062；只接受指定唯一索引，其他唯一键冲突不能当作重复报文。
                if (sqlException.getErrorCode() == 1062 && detail != null
                        && (detail.contains("'sup_business_order.uk_sup_business_order_message_no'")
                        || detail.contains("'uk_sup_business_order_message_no'"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void createAccountChange(
            Long businessOrderId,
            AccountOpeningData request,
            String requestJson) {

        SupAccountChange change =
                new SupAccountChange();

        change.setBusinessOrderId(
                businessOrderId
        );

        change.setPlatformFundNo(
                request.platformFundNo
        );

        change.setCommunityNo(
                request.communityNo
        );

        change.setAccountType(
                request.accountType
        );

        change.setRequestData(
                requestJson
        );

        int affectedRows =
                accountChangeMapper.insertSelective(change);

        assertOneRowUpdated(
                affectedRows,
                "保存9103开户申请"
        );
    }

    private void validateRepeatedRequest(
            SupBusinessOrder existingOrder,
            String businessKey,
            String requestJson) {

        if (existingOrder.getId() == null) {
            throw new IllegalStateException(
                    "已存在的业务主单缺少主键ID"
            );
        }

        if (!TRANSACTION_CODE.equals(
                existingOrder.getTransactionCode())) {

            throw new IllegalArgumentException(
                    "同一报文编号已被其他交易使用"
            );
        }

        if (!businessKey.equals(
                existingOrder.getBusinessKey())) {

            throw new IllegalArgumentException(
                    "同一报文编号对应的业务识别键不一致"
            );
        }

        SupAccountChange existingChange =
                accountChangeMapper
                        .selectByBusinessOrderId(
                                existingOrder.getId()
                        );

        if (existingChange == null) {
            throw new IllegalStateException(
                    "业务主单缺少对应的开户申请记录"
            );
        }

        if (!requestJson.equals(
                existingChange.getRequestData())) {

            throw new IllegalArgumentException(
                    "同一报文编号对应的开户申请内容不一致"
            );
        }
    }

    private String serializeRequest(
            AccountOpeningData request) {

        Map<String, String> values =
                new LinkedHashMap<String, String>();

        values.put(
                "actionCode",
                ACTION_CODE
        );

        values.put(
                "platformFundNo",
                request.platformFundNo
        );

        values.put(
                "accountType",
                request.accountType
        );

        values.put(
                "communityNo",
                request.communityNo
        );

        values.put(
                "mixedCommunityFlag",
                request.mixedCommunityFlag
        );

        values.put(
                "communityAccountNo1",
                request.communityAccountNo1
        );

        values.put(
                "communityAccountBankNo1",
                request.communityAccountBankNo1
        );

        values.put(
                "communityAccountNo2",
                request.communityAccountNo2
        );

        values.put(
                "communityAccountBankNo2",
                request.communityAccountBankNo2
        );

        values.put(
                "propertyCompanyName",
                request.propertyCompanyName
        );

        values.put(
                "propertyCreditCode",
                request.propertyCreditCode
        );

        try {
            return objectMapper.writeValueAsString(
                    values
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "序列化9103开户申请失败",
                    exception
            );
        }
    }

    private static String createBusinessKey(
            AccountOpeningData request) {

        return request.platformFundNo
                + ":"
                + request.communityNo
                + ":"
                + request.accountType;
    }

    private static String requireField(
            ProtocolSegment segment,
            int fieldNo,
            String fieldName) {

        String value =
                segment.getFields().get(fieldNo);

        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName
                            + " Field "
                            + fieldNo
                            + " 不能为空"
            );
        }

        return value;
    }

    private static void requireValue(
            ProtocolSegment segment,
            int fieldNo,
            String expected,
            String fieldName) {

        String actual =
                requireField(
                        segment,
                        fieldNo,
                        fieldName
                );

        if (!expected.equals(actual)) {
            throw new IllegalArgumentException(
                    fieldName
                            + "必须为"
                            + expected
                            + "，实际为"
                            + actual
            );
        }
    }

    private static void requireSameValue(
            ProtocolSegment summary,
            ProtocolSegment child,
            int fieldNo,
            String fieldName) {

        String summaryValue =
                requireField(
                        summary,
                        fieldNo,
                        "汇总报文" + fieldName
                );

        String childValue =
                requireField(
                        child,
                        fieldNo,
                        "子报文" + fieldName
                );

        if (!summaryValue.equals(childValue)) {
            throw new IllegalArgumentException(
                    fieldName
                            + "在汇总报文和子报文中不一致"
            );
        }
    }

    private static void requireOneOf(
            String actual,
            String fieldName,
            String first,
            String second) {

        if (!first.equals(actual)
                && !second.equals(actual)) {

            throw new IllegalArgumentException(
                    fieldName
                            + "只允许"
                            + first
                            + "或"
                            + second
                            + "，实际为"
                            + actual
            );
        }
    }

    private static void requireExactFields(
            ProtocolSegment segment,
            Set<Integer> expected,
            String segmentName) {

        Set<Integer> actual =
                new TreeSet<Integer>(
                        segment.getFields().keySet()
                );

        if (!expected.equals(actual)) {
            throw new IllegalArgumentException(
                    segmentName
                            + "字段集合不正确，期望="
                            + expected
                            + "，实际="
                            + actual
            );
        }
    }

    private static Set<Integer> fieldNumbers(
            Integer... fieldNumbers) {

        return Collections.unmodifiableSet(
                new TreeSet<Integer>(
                        Arrays.asList(fieldNumbers)
                )
        );
    }

    private static void assertOneRowUpdated(
            int affectedRows,
            String operation) {

        if (affectedRows != 1) {
            throw new IllegalStateException(
                    operation
                            + "失败，影响行数="
                            + affectedRows
            );
        }
    }

    private static final class AccountOpeningData {

        private final String messageNo;
        private final String platformFundNo;
        private final String accountType;
        private final String communityNo;
        private final String mixedCommunityFlag;
        private final String communityAccountNo1;
        private final String communityAccountBankNo1;
        private final String communityAccountNo2;
        private final String communityAccountBankNo2;
        private final String propertyCompanyName;
        private final String propertyCreditCode;

        private AccountOpeningData(
                String messageNo,
                String platformFundNo,
                String accountType,
                String communityNo,
                String mixedCommunityFlag,
                String communityAccountNo1,
                String communityAccountBankNo1,
                String communityAccountNo2,
                String communityAccountBankNo2,
                String propertyCompanyName,
                String propertyCreditCode) {

            this.messageNo = messageNo;
            this.platformFundNo = platformFundNo;
            this.accountType = accountType;
            this.communityNo = communityNo;
            this.mixedCommunityFlag =
                    mixedCommunityFlag;
            this.communityAccountNo1 =
                    communityAccountNo1;
            this.communityAccountBankNo1 =
                    communityAccountBankNo1;
            this.communityAccountNo2 =
                    communityAccountNo2;
            this.communityAccountBankNo2 =
                    communityAccountBankNo2;
            this.propertyCompanyName =
                    propertyCompanyName;
            this.propertyCreditCode =
                    propertyCreditCode;
        }
    }
}
