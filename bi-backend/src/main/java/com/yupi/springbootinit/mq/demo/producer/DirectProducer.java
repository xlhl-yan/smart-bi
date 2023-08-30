package com.yupi.springbootinit.mq.demo.producer;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @author xlhl
 */
public class DirectProducer {

    private static final String EXCHANGE_NAME = "direct.exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.225.128");
        factory.setUsername("root");
        factory.setPassword("root");
        factory.setPort(5672);
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String userInput = scanner.nextLine();
                String[] split = userInput.split(" ");
                if (split.length < 1) {
                    continue;
                }
                String routingKey = split[1];
                String message = split[0];

                channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + message + "with routing:'" + routingKey + "'");
            }
        }
    }
}