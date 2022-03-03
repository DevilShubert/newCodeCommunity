package com.example.newcode.serviceTest;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class KaptchaTest {

    @Autowired
    DefaultKaptcha defaultKaptcha;

    @Test
    public void kaptchaTest(){
        System.out.println(defaultKaptcha.createText());
    }
}
