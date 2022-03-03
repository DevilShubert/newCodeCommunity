package com.example.newcode.serviceTest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.newcode.entity.Comment;
import com.example.newcode.entity.DiscussPost;
import com.example.newcode.service.CommentService;
import com.example.newcode.service.DiscussPostService;
import com.example.newcode.util.constant.CommunityConstant;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest
@Slf4j
public class CommentServiceTest implements CommunityConstant {

    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    CommentService commentService;

    @Test
    public void commentCountTest(){
        int count = commentService.selectCountByEntity(1, 228);
        log.info("count is: " + count);
    }

    @Test
    public void commentSelectTest(){
        Page<Comment> commentPage = commentService.selectCommentsByEntity(1, 228, 2, 5);
        List<Comment> records = commentPage.getRecords();
        for (Comment record : records) {
            System.out.println(record);
        }
    }

    @Test
    public void insertTest(){
        Comment comment = new Comment();
        comment.setUserId(161);
        comment.setEntityType(ENTITY_TARGET_POST);
        comment.setEntityId(284);
        comment.setTargetId(0);
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        comment.setContent("comment test");

        DiscussPost discussPost = discussPostService.selectDiscussPostsByPostID(comment.getEntityId());

        commentService.insertComment(comment, discussPost);
    }


}
