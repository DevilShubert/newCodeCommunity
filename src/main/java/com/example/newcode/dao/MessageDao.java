package com.example.newcode.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.newcode.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageDao extends BaseMapper<Message> {
}
