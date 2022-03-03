package com.example.newcode.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.newcode.entity.Comment;
import com.example.newcode.entity.DiscussPost;

import java.util.List;

public interface CommentService extends IService<Comment> {

    /**
     * Get one comment by entityType and entityId
     * @param entityType 评论目标类别
     * @param entityId 具体的评论目标的id
     * @return
     */
    int selectCountByEntity(int entityType, int entityId);


    /**
     * select comments by entityType and entityId with pagination
     * @param entityType 评论目标类别
     * @param entityId 具体的评论目标的id
     * @param curPage 当前页
     * @param pageSize 每页多少个
     * @return
     */
    Page<Comment> selectCommentsByEntity(int entityType, int entityId, int curPage, int pageSize);

    Boolean insertComment(Comment comment, DiscussPost discussPost);
}
