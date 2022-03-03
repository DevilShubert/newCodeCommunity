package com.example.newcode.serviceTest;

import com.example.newcode.util.SensitiveFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class SensitiveFilterTest {
    @Autowired
    SensitiveFilter sensitiveFilter;

    @Test
    public void test(){

      log.info(sensitiveFilter.filter("fabc"));
      log.info(sensitiveFilter.filter("这里有吸毒,这里有嫖娼，这里有赌博"));
    }

}
