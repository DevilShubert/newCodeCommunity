package com.example.newcode.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.newcode.entity.DiscussPost;
import com.example.newcode.entity.User;

public interface DiscussPostService extends IService<DiscussPost> {
    IPage<DiscussPost> selectMapsPage(int userID, int curPage, int pageSize);
    Integer selectDiscussPosts(int userID);
}
