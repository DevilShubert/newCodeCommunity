package com.example.newcode.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.newcode.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentDao extends BaseMapper<Comment> {
}
