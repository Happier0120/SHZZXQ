package com.example.propertysupervision.persistence.entity;

import java.time.LocalDateTime;

/**
 * 监管账户开户及变更历史表
 * @TableName sup_account_change
 */
public class SupAccountChange {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 业务主单ID
     */
    private Long businessOrderId;

    /**
     * 监管账户ID
     */
    private Long accountId;

    /**
     * 维修资金平台标识
     */
    private String platformFundNo;

    /**
     * 小区平台唯一编号
     */
    private String communityNo;

    /**
     * 监管账户类型
     */
    private String accountType;

    /**
     * 变更前账户快照
     */
    private String beforeData;

    /**
     * 开户或变更申请数据
     */
    private String requestData;

    /**
     * 处理后的账户快照
     */
    private String afterData;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 主键ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 主键ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 业务主单ID
     */
    public Long getBusinessOrderId() {
        return businessOrderId;
    }

    /**
     * 业务主单ID
     */
    public void setBusinessOrderId(Long businessOrderId) {
        this.businessOrderId = businessOrderId;
    }

    /**
     * 监管账户ID
     */
    public Long getAccountId() {
        return accountId;
    }

    /**
     * 监管账户ID
     */
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    /**
     * 维修资金平台标识
     */
    public String getPlatformFundNo() {
        return platformFundNo;
    }

    /**
     * 维修资金平台标识
     */
    public void setPlatformFundNo(String platformFundNo) {
        this.platformFundNo = platformFundNo;
    }

    /**
     * 小区平台唯一编号
     */
    public String getCommunityNo() {
        return communityNo;
    }

    /**
     * 小区平台唯一编号
     */
    public void setCommunityNo(String communityNo) {
        this.communityNo = communityNo;
    }

    /**
     * 监管账户类型
     */
    public String getAccountType() {
        return accountType;
    }

    /**
     * 监管账户类型
     */
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    /**
     * 变更前账户快照
     */
    public String getBeforeData() {
        return beforeData;
    }

    /**
     * 变更前账户快照
     */
    public void setBeforeData(String beforeData) {
        this.beforeData = beforeData;
    }

    /**
     * 开户或变更申请数据
     */
    public String getRequestData() {
        return requestData;
    }

    /**
     * 开户或变更申请数据
     */
    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    /**
     * 处理后的账户快照
     */
    public String getAfterData() {
        return afterData;
    }

    /**
     * 处理后的账户快照
     */
    public void setAfterData(String afterData) {
        this.afterData = afterData;
    }

    /**
     * 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 更新时间
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 更新时间
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}