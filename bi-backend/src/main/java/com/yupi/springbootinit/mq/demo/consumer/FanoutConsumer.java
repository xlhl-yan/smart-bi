package com.yupi.springbootinit.mq.demo.consumer;

import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;

/**
 * @author xlhl
 */
public class FanoutConsumer {
    private static final String EXCHANGE_NAME = "fanout_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.225.128");
        factory.setUsername("root");
        factory.setPassword("root");
        factory.setPort(5672);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //  声明交换机
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

        //  创建队列
        String queueName1 = "helena_queue";
        channel.queueDeclare(queueName1,false,false,false,null);
        channel.queueBind(queueName1, EXCHANGE_NAME, "");

        String queueName2 = "xlhl_queue";
        channel.queueDeclare(queueName2,false,false,false,null);
        channel.queueBind(queueName2, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [海伦娜] Received '" + message + "'");
        };

        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [指挥官] Received '" + message + "'");
        };
        channel.basicConsume(queueName2, true, deliverCallback2, consumerTag -> {
        });
        channel.basicConsume(queueName1, true, deliverCallback1, consumerTag -> {
        });
    }
}