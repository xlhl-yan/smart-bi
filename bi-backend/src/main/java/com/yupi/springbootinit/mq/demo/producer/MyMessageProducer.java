package com.yupi.springbootinit.mq.demo.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * MyMessageProducer
 *
 * @author xlhl
 * @version 1.0
 * @description Producer 生产者
 */
@Component
public class MyMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String exchange, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
