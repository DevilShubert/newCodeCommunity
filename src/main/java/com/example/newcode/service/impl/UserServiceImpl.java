package com.example.newcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.newcode.dao.UserDao;
import com.example.newcode.entity.User;
import com.example.newcode.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService   {

    @Autowired
    private UserDao userDao;

    @Override
    public User selectById(int id) {
        User user = userDao.selectById(id);
        return user;
    }

    @Override
    public List<User> selectByName(String name){
        Map<String, Object> map =  new HashMap<String, Object>();
        map.put("username", name);
        List<User> users = userDao.selectByMap(map);
        return users;
    }

    @Override
    public List<User> selectByEmail(String email){
        Map<String, Object> map =  new HashMap<String, Object>();
        map.put("email", email);
        List<User> users = userDao.selectByMap(map);
        return users;
    }

    @Override
    public Boolean insertUser(User user) {
        return userDao.insert(user) > 0;
    }

    @Override
    public Boolean updateStatus(int id, int status) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        User user = new User();
        user.setStatus(status);
        return userDao.update(user, updateWrapper) > 0;
    }

    @Override
    public Boolean updateHeader(int id, String headerUrl) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        User user = new User();
        user.setHeaderUrl(headerUrl);
        return userDao.update(user, updateWrapper) > 0;
    }

    @Override
    public Boolean updatePassword(int id, String password) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        User user = new User();
        user.setPassword(password);
        return userDao.update(user, updateWrapper) > 0;
    }


}
