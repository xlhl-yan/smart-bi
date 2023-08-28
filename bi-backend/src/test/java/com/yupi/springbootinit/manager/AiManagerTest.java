package com.yupi.springbootinit.manager;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class AiManagerTest {

    @Resource
    private AiManager aiManager;



    @Test
    void doChat() {
        String hello = aiManager.doChat(1695768463945039874L, "你好");

        assert StringUtils.isNotBlank(hello);
        System.out.println("hello = " + hello);
    }
}