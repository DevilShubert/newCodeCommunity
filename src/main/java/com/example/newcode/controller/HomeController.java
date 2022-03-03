package com.example.newcode.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.newcode.entity.DiscussPost;
import com.example.newcode.entity.MyPage;
import com.example.newcode.entity.User;
import com.example.newcode.service.DiscussPostService;
import com.example.newcode.service.UserService;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    UserService userService;

    @RequestMapping(value = "/indexPage", method = RequestMethod.GET)
    public String getIndexPage(Model model, MyPage page){
        // MVC帮忙实现了类Mypage
        // 当第一次访问indexPage页面时，是没有提交任何page数据的，但是MVC会自动实例化一个对象
        IPage<DiscussPost> postIPage = discussPostService.selectMapsPage(0, page.getCurrent(), page.getLimit());
        int size = postIPage.getRecords().size();

        // 设置总页数
        page.setTotal((int)postIPage.getPages());
        // 设置总记录数
        page.setRows(size);
        // 设置访问路径
        page.setPath("/indexPage");

        List<Map<String, Object>> discussPosts = new ArrayList<>();
        List<DiscussPost> postList = postIPage.getRecords();
        if (postList.size() != 0){
            for (DiscussPost post : postList) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.selectById(post.getUserId());
                map.put("user", user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("page", page);
        return "index";
    }




}
