package com.example.newcode.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.newcode.entity.User;

import java.util.List;


public interface UserService extends IService<User> {
    public List<User> selectByName(String name);

    public User selectById(int id);

    List<User> selectByEmail(String email);

    Boolean insertUser(User user);

    Boolean updateStatus(int id, int status, User user);

    Boolean updateHeader(int id, String headerUrl, User user);

    Boolean updatePassword(int id, String password, User user);


}
