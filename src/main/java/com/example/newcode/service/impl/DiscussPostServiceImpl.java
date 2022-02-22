package com.example.newcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.newcode.dao.DiscussPostDao;
import com.example.newcode.dao.UserDao;
import com.example.newcode.entity.DiscussPost;
import com.example.newcode.entity.User;
import com.example.newcode.service.DiscussPostService;
import com.example.newcode.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiscussPostServiceImpl extends ServiceImpl<DiscussPostDao, DiscussPost> implements DiscussPostService {

    @Autowired
    DiscussPostDao discussPostDao;

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

        wrapper.eq(userID!=0,"user_id",userID);
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
    public Integer selectDiscussPosts(int userID) {
        QueryWrapper<DiscussPost> wrapper = new QueryWrapper<>();
        wrapper.eq(userID!=0,"user_id",userID);
        wrapper.le("status",1);
        return discussPostDao.selectCount(wrapper);
    }
}
