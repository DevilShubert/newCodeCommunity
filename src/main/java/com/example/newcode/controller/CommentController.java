package com.example.newcode.controller;

import com.example.newcode.annotation.LoginRequired;
import com.example.newcode.entity.Comment;
import com.example.newcode.entity.DiscussPost;
import com.example.newcode.service.CommentService;
import com.example.newcode.service.DiscussPostService;
import com.example.newcode.util.CommunityUtils;
import com.example.newcode.util.HostHolder;
import com.example.newcode.util.SensitiveFilter;
import com.example.newcode.util.constant.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController  {


    @Autowired
    HostHolder hostHolder;

    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    CommentService commentService;



    /**
     * add new Comment element
     * @param postID 帖子id
     * @param comment 评论实体
     */
    @RequestMapping(value = "/add/{discussPostId}" ,method = RequestMethod.POST)
    @LoginRequired
    public String addComment(@PathVariable("discussPostId")int postID, Comment comment){
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // comment实体默认会有entityType、entityId、content（targetId）
        // 对于targetId属性，如果默认是0，如果此条评论是对某个用户的回复，则targetId=1
        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);

        // locate discussPost
        DiscussPost discussPost = discussPostService.selectDiscussPostsByPostID(postID);

        // do insert
        commentService.insertComment(comment, discussPost);
        return "redirect:/discuss/detail/" + postID;
    }
}
