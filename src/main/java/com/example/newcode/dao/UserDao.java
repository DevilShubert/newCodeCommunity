package com.example.newcode.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.newcode.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao extends BaseMapper<User> {
}
