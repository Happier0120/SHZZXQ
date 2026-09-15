package com.example.propertysupervision.persistence.entity;

import java.time.LocalDateTime;

/**
 * 监管业务主单表
 * @TableName sup_business_order
 */
public class SupBusinessOrder {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 发起报文编号
     */
    private String initiatingMessageNo;

    /**
     * 交易代码
     */
    private String transactionCode;

    /**
     * 业务识别键
     */
    private String businessKey;

    /**
     * 0物业中心，1银行
     */
    private String initiator;

    /**
     * 0待处理，1处理中，2成功，3失败
     */
    private String status;

    /**
     * 关联原业务主单ID
     */
    private Long relatedBusinessOrderId;

    /**
     * 业务发起时间
     */
    private LocalDateTime startedAt;

    /**
     * 业务完成时间
     */
    private LocalDateTime finishedAt;

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
     * 发起报文编号
     */
    public String getInitiatingMessageNo() {
        return initiatingMessageNo;
    }

    /**
     * 发起报文编号
     */
    public void setInitiatingMessageNo(String initiatingMessageNo) {
        this.initiatingMessageNo = initiatingMessageNo;
    }

    /**
     * 交易代码
     */
    public String getTransactionCode() {
        return transactionCode;
    }

    /**
     * 交易代码
     */
    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    /**
     * 业务识别键
     */
    public String getBusinessKey() {
        return businessKey;
    }

    /**
     * 业务识别键
     */
    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    /**
     * 0物业中心，1银行
     */
    public String getInitiator() {
        return initiator;
    }

    /**
     * 0物业中心，1银行
     */
    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    /**
     * 0待处理，1处理中，2成功，3失败
     */
    public String getStatus() {
        return status;
    }

    /**
     * 0待处理，1处理中，2成功，3失败
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 关联原业务主单ID
     */
    public Long getRelatedBusinessOrderId() {
        return relatedBusinessOrderId;
    }

    /**
     * 关联原业务主单ID
     */
    public void setRelatedBusinessOrderId(Long relatedBusinessOrderId) {
        this.relatedBusinessOrderId = relatedBusinessOrderId;
    }

    /**
     * 业务发起时间
     */
    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    /**
     * 业务发起时间
     */
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    /**
     * 业务完成时间
     */
    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    /**
     * 业务完成时间
     */
    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
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