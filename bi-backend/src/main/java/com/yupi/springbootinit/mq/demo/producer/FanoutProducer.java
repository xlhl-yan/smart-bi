package com.yupi.springbootinit.mq.demo.producer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @author xlhl
 */
public class FanoutProducer {

    private static final String EXCHANGE_NAME = "fanout_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.225.128");
        factory.setUsername("root");
        factory.setPassword("root");
        factory.setPort(5672);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            //  创建交换机
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String message = scanner.nextLine();
                channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

                channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + message + "'");
            }
        }
    }
}