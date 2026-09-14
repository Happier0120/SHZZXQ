package com.example.propertysupervision.persistence.mapper;

import com.example.propertysupervision.persistence.entity.SupMessage;

/**
* @author 19828
* @description 针对表【sup_message(监管报文流水表)】的数据库操作Mapper
* @createDate 2026-09-14 20:08:25
* @Entity com.example.propertysupervision.persistence.entity.SupMessage
*/
public interface SupMessageMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SupMessage record);

    int insertSelective(SupMessage record);

    SupMessage selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SupMessage record);

    int updateByPrimaryKey(SupMessage record);

}
