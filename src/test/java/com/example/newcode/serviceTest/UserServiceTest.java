package com.example.newcode.serviceTest;

import com.example.newcode.entity.User;
import com.example.newcode.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    UserService userService;

    @Test
    public void userTest(){
        User users1 = userService.getById(11);
        System.out.println(users1);

        List<User> users2 = userService.selectByName("nowcoder11");
        System.out.println(users2);
        for (User user:users2) {
            userService.updateStatus(149, 0, user);
        }

    }

    @Test
    public void inserTest(){
        User user = new User();
        user.setUsername("TEST");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("1wy385@qq.com");
        user.setHeaderUrl("http://images.nowcoder.com/head/896t.png");
        userService.insertUser(user);
        System.out.println(userService.selectByName("TEST"));
    }

}
