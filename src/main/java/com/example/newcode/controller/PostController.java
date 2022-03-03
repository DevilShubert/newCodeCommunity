package com.example.newcode.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.newcode.annotation.LoginRequired;
import com.example.newcode.entity.Comment;
import com.example.newcode.entity.DiscussPost;
import com.example.newcode.entity.MyPage;
import com.example.newcode.entity.User;
import com.example.newcode.service.CommentService;
import com.example.newcode.service.DiscussPostService;
import com.example.newcode.service.UserService;
import com.example.newcode.util.CommunityUtils;
import com.example.newcode.util.HostHolder;
import com.example.newcode.util.constant.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class PostController implements CommunityConstant {
    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    /**
     * Insert one Post
     * @param title
     * @param content
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String insertPost(String title, String content){
        DiscussPost post = new DiscussPost();
        post.setUserId(hostHolder.getUser().getId());
        post.setTitle(title);
        post.setContent(content);
        post.setType(0);
        post.setStatus(0);
        post.setCreateTime(new Date());
        post.setCommentCount(0);
        post.setScore(0.0);

        discussPostService.addDiscussPost(post);
        // In case of error, it will be handled uniformly in the future
        return CommunityUtils.getJSONString(0, "发布成功!");
    }

    @RequestMapping(value = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int postId, Model model, MyPage myPage){
        // 帖子
        DiscussPost post = discussPostService.selectDiscussPostsByPostID(postId);
        int userId = post.getUserId();
        // 作者
        User user = userService.selectById(userId);
        model.addAttribute("post", post);
        model.addAttribute("user", user);

        // 设置帖子单页评论数量
        myPage.setLimit(5);
        // 设置访问路径：/detail/{discussPostId}
        myPage.setPath("/discuss/detail/" + postId);
        // 拿到对于帖子的评论数
        myPage.setRows(post.getCommentCount());

        // 包装对帖子的评论
        List<Map<String, Object>> commentVoList = new ArrayList<>();

        // 对帖子的评论
        Page<Comment> commentPage = commentService.
                selectCommentsByEntity(ENTITY_TARGET_POST, postId, myPage.getCurrent(), myPage.getLimit());
        List<Comment> commentList = commentPage.getRecords();
        // 设置总页数
        myPage.setTotal((int)commentPage.getPages());
        if (commentList != null) {
            for (Comment comment : commentList) {
                HashMap<String, Object> commentsVo = new HashMap<>();
                commentsVo.put("comment", comment);
                commentsVo.put("user", userService.selectById(comment.getUserId()));
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                // 帖子中评论的回复comment列表（对于帖子中评论的回复comment就不再需要分页的操作）
                Page<Comment> replayPages = commentService.
                        selectCommentsByEntity(ENTITY_TARGET_COMMENT, comment.getId(), 1, Integer.MAX_VALUE);
                List<Comment> replayList = replayPages.getRecords();
                if (replayList != null) {
                    for (Comment replayComment : replayList) {
                        HashMap<String, Object> replayVo = new HashMap<String, Object>();
                        // 评论的comment实体
                        replayVo.put("replay", replayComment);
                        // replayComment的作者
                        replayVo.put("user", userService.selectById(replayComment.getUserId()));
                        // replayComment回复的目标：是对用户回复还是对帖子的评论回复
                        User target = (replayComment.getTargetId() != 0) ?
                                userService.selectById(replayComment.getTargetId()) : null;
                        replayVo.put("target", target);
                        replyVoList.add(replayVo);
                    }
                }
                commentsVo.put("replays", replyVoList);
                // 回复（评论的comment）数量
                commentsVo.put("replayCount", commentService.selectCountByEntity(ENTITY_TARGET_COMMENT, comment.getId()));
                commentVoList.add(commentsVo);
            }
        }
        model.addAttribute("comments", commentVoList);
        model.addAttribute("page", myPage);
        return "/site/discuss-detail";
    }
}
