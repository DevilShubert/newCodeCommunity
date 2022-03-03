package com.example.newcode.service;

import com.example.newcode.entity.User;

import java.util.Map;

public interface UserRegisterService {
    Map<String, Object> doRegister(User user);

    int activation(int userId, String code);
}
