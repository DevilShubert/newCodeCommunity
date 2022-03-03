package com.example.newcode.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.newcode.entity.DiscussPost;
import com.example.newcode.entity.User;

public interface DiscussPostService extends IService<DiscussPost> {
    /**
     * select all posts by pagination
     * @param userID 用户id
     * @param curPage 当前页号
     * @param pageSize 一页的大小
     * @return
     */
    IPage<DiscussPost> selectMapsPage(int userID, int curPage, int pageSize);

    /**
     * select one post by userID
     * @param userID 用户ID
     * @return
     */
    DiscussPost selectDiscussPostsByUserID(int userID);


    /**
     * select one post by postID
     * @param postID 用户ID
     * @return
     */
    DiscussPost selectDiscussPostsByPostID(int postID);

    /**
     * add new post
     * @param post 帖子实体
     * @return
     */
    Integer addDiscussPost(DiscussPost post);


    Boolean updateCommentCount(DiscussPost post, int count);
}
