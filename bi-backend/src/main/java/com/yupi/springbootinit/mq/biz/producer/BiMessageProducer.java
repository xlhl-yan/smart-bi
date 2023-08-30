package com.yupi.springbootinit.mq.biz.producer;

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
public class BiMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 将消息发送至对应的队列中
     *
     * @param exchange   交换机名称
     * @param routingKey routingKey
     * @param message    消息
     */
    public void sendMessage(String exchange, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }


}
