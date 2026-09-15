package com.example.propertysupervision.persistence.mapper;

import com.example.propertysupervision.persistence.entity.SupBusinessOrder;

/**
* @author 19828
* @description 针对表【sup_business_order(监管业务主单表)】的数据库操作Mapper
* @createDate 2026-09-15 10:57:08
* @Entity com.example.propertysupervision.persistence.entity.SupBusinessOrder
*/
public interface SupBusinessOrderMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SupBusinessOrder record);

    int insertSelective(SupBusinessOrder record);

    SupBusinessOrder selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SupBusinessOrder record);

    int updateByPrimaryKey(SupBusinessOrder record);

    SupBusinessOrder selectByInitiatingMessageNo(
            String initiatingMessageNo
    );

}
