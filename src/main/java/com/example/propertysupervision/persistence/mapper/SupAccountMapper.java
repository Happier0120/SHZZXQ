package com.example.propertysupervision.persistence.mapper;

import com.example.propertysupervision.persistence.entity.SupAccount;

/**
* @author 19828
* @description 针对表【sup_account(监管账户主档表)】的数据库操作Mapper
* @createDate 2026-09-15 10:57:33
* @Entity com.example.propertysupervision.persistence.entity.SupAccount
*/
public interface SupAccountMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SupAccount record);

    int insertSelective(SupAccount record);

    SupAccount selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SupAccount record);

    int updateByPrimaryKey(SupAccount record);

}
