package com.example.propertysupervision.persistence.entity;

import java.time.LocalDateTime;

/**
 * 监管报文流水表
 * @TableName sup_message
 */
public class SupMessage {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 业务订单ID
     */
    private Long businessOrderId;

    /**
     * 报文编号
     */
    private String messageNo;

    /**
     * 交易代码
     */
    private String transactionCode;

    /**
     * 收发方向：I银行接收，O银行发送
     */
    private String direction;

    /**
     * 汇总报文类型
     */
    private String summaryType;

    /**
     * 子报文类型
     */
    private String childType;

    /**
     * 报文处理状态：0处理中，1成功，2失败
     */
    private String status;

    /**
     * 报文处理结果
     */
    private String responseCode;

    /**
     * 报文处理信息
     */
    private String responseMessage;

    /**
     * 系统异常信息
     */
    private String errorMessage;

    /**
     * 报文实际收发时间
     */
    private LocalDateTime messageTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 完整原始报文字节
     */
    private byte[] rawMessage;

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
     * 业务订单ID
     */
    public Long getBusinessOrderId() {
        return businessOrderId;
    }

    /**
     * 业务订单ID
     */
    public void setBusinessOrderId(Long businessOrderId) {
        this.businessOrderId = businessOrderId;
    }

    /**
     * 报文编号
     */
    public String getMessageNo() {
        return messageNo;
    }

    /**
     * 报文编号
     */
    public void setMessageNo(String messageNo) {
        this.messageNo = messageNo;
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
     * 收发方向：I银行接收，O银行发送
     */
    public String getDirection() {
        return direction;
    }

    /**
     * 收发方向：I银行接收，O银行发送
     */
    public void setDirection(String direction) {
        this.direction = direction;
    }

    /**
     * 汇总报文类型
     */
    public String getSummaryType() {
        return summaryType;
    }

    /**
     * 汇总报文类型
     */
    public void setSummaryType(String summaryType) {
        this.summaryType = summaryType;
    }

    /**
     * 子报文类型
     */
    public String getChildType() {
        return childType;
    }

    /**
     * 子报文类型
     */
    public void setChildType(String childType) {
        this.childType = childType;
    }

    /**
     * 报文处理状态：0处理中，1成功，2失败
     */
    public String getStatus() {
        return status;
    }

    /**
     * 报文处理状态：0处理中，1成功，2失败
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 报文处理结果
     */
    public String getResponseCode() {
        return responseCode;
    }

    /**
     * 报文处理结果
     */
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * 报文处理信息
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * 报文处理信息
     */
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    /**
     * 系统异常信息
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 系统异常信息
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 报文实际收发时间
     */
    public LocalDateTime getMessageTime() {
        return messageTime;
    }

    /**
     * 报文实际收发时间
     */
    public void setMessageTime(LocalDateTime messageTime) {
        this.messageTime = messageTime;
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

    /**
     * 完整原始报文字节
     */
    public byte[] getRawMessage() {
        return rawMessage;
    }

    /**
     * 完整原始报文字节
     */
    public void setRawMessage(byte[] rawMessage) {
        this.rawMessage = rawMessage;
    }
}