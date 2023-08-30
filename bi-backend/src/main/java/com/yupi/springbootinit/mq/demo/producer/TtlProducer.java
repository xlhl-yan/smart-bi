package com.yupi.springbootinit.mq.demo.producer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 生产者 demo
 *
 * @author xlhl
 */
public class TtlProducer {

    private final static String QUEUE_NAME = "ttl.queue";

    public static void main(String[] argv) throws Exception {
        //  创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.225.128");
        factory.setUsername("root");
        factory.setPassword("root");
        factory.setPort(5672);

        //  创建连接
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            //  新建消息队列

//            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            //  发送消息

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String message = scanner.nextLine();

                //  给消息指定过期时间
                AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                        .expiration("1000")
                        .build();

                channel.basicPublish("", QUEUE_NAME, properties, message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + message + "'");
            }

        }
    }
}