package com.yupi.springbootinit.mq.demo.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * MyMessageConsumer
 *
 * @author xlhl
 * @version 1.0
 * @description Consumer 消费者
 */
@Component
@Slf4j
public class MyMessageConsumer {

    /**
     * 程序监听的消息队列和确认机制
     *
     * @param message
     * @param channel
     * @param deliveryTag
     * @throws Exception
     */
    @RabbitListener(queues = "test_queue", ackMode = "MANUAL")
    public void onMessage(String message, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws Exception {

        try {
            log.info("message = {}", message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
