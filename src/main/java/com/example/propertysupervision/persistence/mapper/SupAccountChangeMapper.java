package com.example.propertysupervision.persistence.mapper;

import com.example.propertysupervision.persistence.entity.SupAccountChange;

/**
* @author 19828
* @description 针对表【sup_account_change(监管账户开户及变更历史表)】的数据库操作Mapper
* @createDate 2026-09-15 10:57:25
* @Entity com.example.propertysupervision.persistence.entity.SupAccountChange
*/
public interface SupAccountChangeMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SupAccountChange record);

    int insertSelective(SupAccountChange record);

    SupAccountChange selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SupAccountChange record);

    int updateByPrimaryKey(SupAccountChange record);

    SupAccountChange selectByBusinessOrderId(Long businessOrderId);

}
