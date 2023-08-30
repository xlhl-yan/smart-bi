package com.yupi.springbootinit.mq.biz.producer;

import com.yupi.springbootinit.mq.demo.producer.MyMessageProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class MyMessageProducerTest {

    @Resource
    private MyMessageProducer producer;

    @Test
    void sendMessage() {
        producer.sendMessage("test_exchange","test","HelloWorld");
    }
}