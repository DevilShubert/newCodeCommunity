package com.example.newcode.controllerTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class HomeControllerTest {
    @Autowired
    MockMvc mockMvc;

    /**
        * @param
        * @return void
        * @author JLian 测试拿到所有数据
        * @date 2022/2/22 7:38 下午
    */
    @Test
    public void getAll() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/indexPage");
        ResultActions actions = mockMvc.perform(builder);
    }
}
