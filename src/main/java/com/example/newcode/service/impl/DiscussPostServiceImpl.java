package com.example.newcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.newcode.dao.DiscussPostDao;
import com.example.newcode.dao.UserDao;
import com.example.newcode.entity.DiscussPost;
import com.example.newcode.entity.User;
import com.example.newcode.service.DiscussPostService;
import com.example.newcode.service.UserService;
import com.example.newcode.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class DiscussPostServiceImpl extends ServiceImpl<DiscussPostDao, DiscussPost> implements DiscussPostService {

    @Autowired
    DiscussPostDao discussPostDao;

    @Autowired
    SensitiveFilter sensitiveFilter;

    /**
        * @param userID
    	* @param curPage
    	* @param pageSize
        * @return com.baomidou.mybatisplus.core.metadata.IPage<com.example.newcode.entity.DiscussPost>
        * @author JLian 带有条件的分页查询
        * @date 2022/2/22 6:25 下午
    */

    public IPage<DiscussPost> selectMapsPage(int userID, int curPage, int pageSize){

        Page<DiscussPost> postPage = new Page<>(curPage, pageSize);
        QueryWrapper<DiscussPost> wrapper = new QueryWrapper<>();

        wrapper.eq(userID!=0,"user_id", userID);
        wrapper.le("status",1);
        Page<DiscussPost> discussPostPage = discussPostDao.selectPage(postPage, wrapper);
        return discussPostPage;
    }

    /**
        * @param userID
        * @return java.lang.Integer
        * @author JLian 带有条件的查询有多少条帖子
        * @date 2022/2/22 6:25 下午
    */

    @Override
    public DiscussPost selectDiscussPostsByUserID(int userID) {
        QueryWrapper<DiscussPost> wrapper = new QueryWrapper<>();
        wrapper.eq(userID!=0,"user_id",userID);
        wrapper.le("status",1);
        DiscussPost discussPost = discussPostDao.selectOne(wrapper);
        return  discussPost;
    }

    @Override
    public DiscussPost selectDiscussPostsByPostID(int postID) {
        QueryWrapper<DiscussPost> wrapper = new QueryWrapper<>();
        wrapper.eq(postID!=0,"id",postID);
        wrapper.le("status",1);
        DiscussPost discussPost = discussPostDao.selectOne(wrapper);
        return  discussPost;
    }

    @Override
    public Integer addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        // 转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // filter
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        return discussPostDao.insert(post);
    }

    @Override
    public Boolean updateCommentCount(DiscussPost post, int count) {
        UpdateWrapper<DiscussPost> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", post.getId());
        post.setCommentCount(count);
        return discussPostDao.update(post, updateWrapper) > 0;
    }
}
