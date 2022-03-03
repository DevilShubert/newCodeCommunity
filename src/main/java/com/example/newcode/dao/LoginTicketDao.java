package com.example.newcode.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.newcode.entity.LoginTicket;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginTicketDao extends BaseMapper<LoginTicket> {
}
