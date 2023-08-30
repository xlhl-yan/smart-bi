package com.yupi.springbootinit.mq.demo;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * MyInit
 *
 * @author xlhl
 * @version 1.0
 * @description 创建用于测试的交换机与队列，在程序执行前 只需执行一次
 */
public class MyInit {
    private static final String EXCHANGE_NAME = "test_exchange";

    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("192.168.225.128");
            factory.setUsername("root");
            factory.setPassword("root");
            factory.setPort(5672);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

            //  创建队列
            String queueName = "test_queue";
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, EXCHANGE_NAME, "test");

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
