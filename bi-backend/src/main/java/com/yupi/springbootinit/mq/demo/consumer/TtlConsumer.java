package com.yupi.springbootinit.mq.demo.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 消费者 demo
 *
 * @author xlhl
 */
public class TtlConsumer {

    private final static String QUEUE_NAME = "ttl.queue";

    public static void main(String[] argv) throws Exception {
        //  创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.225.128");
        factory.setUsername("root");
        factory.setPassword("root");
        factory.setPort(5672);
        //  创建连接
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //  声明队列    指定消息过期时间
        Map<String, Object> args = new HashMap<>(16);
        args.put("x-message-ttl", 1000 * 5);
        channel.queueDeclare(QUEUE_NAME, false, false, false, args);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        //  定义了如何处理消息
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        };
        //  消费消息，会持续阻塞
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
        });
    }
}