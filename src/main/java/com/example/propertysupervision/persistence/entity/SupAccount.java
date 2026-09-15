package com.example.propertysupervision.persistence.entity;

import java.time.LocalDateTime;

/**
 * 监管账户主档表
 * @TableName sup_account
 */
public class SupAccount {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 维修资金平台标识
     */
    private String platformFundNo;

    /**
     * 小区平台唯一标识
     */
    private String communityNo;

    /**
     * 监管账户类型
     */
    private String accountType;

    /**
     * 是否混合小区
     */
    private String mixedCommunityFlag;

    /**
     * 物业企业名称
     */
    private String propertyCompanyName;

    /**
     * 物业企业统一社会信用代码
     */
    private String propertyCreditCode;

    /**
     * 小区会计账号1
     */
    private String communityAccountNo1;

    /**
     * 小区会计账号1行号
     */
    private String communityAccountBankNo1;

    /**
     * 小区会计账号2
     */
    private String communityAccountNo2;

    /**
     * 小区会计账号2行号
     */
    private String communityAccountBankNo2;

    /**
     * 监管账户户名
     */
    private String regulatoryAccountName;

    /**
     * 监管账户会计账号
     */
    private String regulatoryAccountNo;

    /**
     * 监管账户行号
     */
    private String regulatoryAccountBankNo;

    /**
     * 收款账户开户行名称
     */
    private String receiptBankName;

    /**
     * 收款账户会计账号
     */
    private String receiptAccountNo;

    /**
     * 收款账户行号
     */
    private String receiptAccountBankNo;

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
     * 小区平台唯一标识
     */
    public String getCommunityNo() {
        return communityNo;
    }

    /**
     * 小区平台唯一标识
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
     * 是否混合小区
     */
    public String getMixedCommunityFlag() {
        return mixedCommunityFlag;
    }

    /**
     * 是否混合小区
     */
    public void setMixedCommunityFlag(String mixedCommunityFlag) {
        this.mixedCommunityFlag = mixedCommunityFlag;
    }

    /**
     * 物业企业名称
     */
    public String getPropertyCompanyName() {
        return propertyCompanyName;
    }

    /**
     * 物业企业名称
     */
    public void setPropertyCompanyName(String propertyCompanyName) {
        this.propertyCompanyName = propertyCompanyName;
    }

    /**
     * 物业企业统一社会信用代码
     */
    public String getPropertyCreditCode() {
        return propertyCreditCode;
    }

    /**
     * 物业企业统一社会信用代码
     */
    public void setPropertyCreditCode(String propertyCreditCode) {
        this.propertyCreditCode = propertyCreditCode;
    }

    /**
     * 小区会计账号1
     */
    public String getCommunityAccountNo1() {
        return communityAccountNo1;
    }

    /**
     * 小区会计账号1
     */
    public void setCommunityAccountNo1(String communityAccountNo1) {
        this.communityAccountNo1 = communityAccountNo1;
    }

    /**
     * 小区会计账号1行号
     */
    public String getCommunityAccountBankNo1() {
        return communityAccountBankNo1;
    }

    /**
     * 小区会计账号1行号
     */
    public void setCommunityAccountBankNo1(String communityAccountBankNo1) {
        this.communityAccountBankNo1 = communityAccountBankNo1;
    }

    /**
     * 小区会计账号2
     */
    public String getCommunityAccountNo2() {
        return communityAccountNo2;
    }

    /**
     * 小区会计账号2
     */
    public void setCommunityAccountNo2(String communityAccountNo2) {
        this.communityAccountNo2 = communityAccountNo2;
    }

    /**
     * 小区会计账号2行号
     */
    public String getCommunityAccountBankNo2() {
        return communityAccountBankNo2;
    }

    /**
     * 小区会计账号2行号
     */
    public void setCommunityAccountBankNo2(String communityAccountBankNo2) {
        this.communityAccountBankNo2 = communityAccountBankNo2;
    }

    /**
     * 监管账户户名
     */
    public String getRegulatoryAccountName() {
        return regulatoryAccountName;
    }

    /**
     * 监管账户户名
     */
    public void setRegulatoryAccountName(String regulatoryAccountName) {
        this.regulatoryAccountName = regulatoryAccountName;
    }

    /**
     * 监管账户会计账号
     */
    public String getRegulatoryAccountNo() {
        return regulatoryAccountNo;
    }

    /**
     * 监管账户会计账号
     */
    public void setRegulatoryAccountNo(String regulatoryAccountNo) {
        this.regulatoryAccountNo = regulatoryAccountNo;
    }

    /**
     * 监管账户行号
     */
    public String getRegulatoryAccountBankNo() {
        return regulatoryAccountBankNo;
    }

    /**
     * 监管账户行号
     */
    public void setRegulatoryAccountBankNo(String regulatoryAccountBankNo) {
        this.regulatoryAccountBankNo = regulatoryAccountBankNo;
    }

    /**
     * 收款账户开户行名称
     */
    public String getReceiptBankName() {
        return receiptBankName;
    }

    /**
     * 收款账户开户行名称
     */
    public void setReceiptBankName(String receiptBankName) {
        this.receiptBankName = receiptBankName;
    }

    /**
     * 收款账户会计账号
     */
    public String getReceiptAccountNo() {
        return receiptAccountNo;
    }

    /**
     * 收款账户会计账号
     */
    public void setReceiptAccountNo(String receiptAccountNo) {
        this.receiptAccountNo = receiptAccountNo;
    }

    /**
     * 收款账户行号
     */
    public String getReceiptAccountBankNo() {
        return receiptAccountBankNo;
    }

    /**
     * 收款账户行号
     */
    public void setReceiptAccountBankNo(String receiptAccountBankNo) {
        this.receiptAccountBankNo = receiptAccountBankNo;
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