package com.yupi.springbootinit.mq.biz;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.yupi.springbootinit.constant.RabbitmqConstant;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * MyInit
 *
 * @author xlhl
 * @version 1.0
 * @description 创建用于交换机与队列，在程序执行前 只需执行一次
 */
public class BiInitMain {

    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("192.168.225.128");
            factory.setUsername("root");
            factory.setPassword("root");
            factory.setPort(5672);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(RabbitmqConstant.BI_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

            //  创建队列
            channel.queueDeclare(RabbitmqConstant.BI_QUEUE_NAME, true, false, false, null);

            channel.queueBind(RabbitmqConstant.BI_QUEUE_NAME, RabbitmqConstant.BI_EXCHANGE_NAME, RabbitmqConstant.BI_ROUTING_KEY);

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
