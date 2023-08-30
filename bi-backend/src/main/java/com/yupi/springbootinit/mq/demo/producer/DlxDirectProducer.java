package com.yupi.springbootinit.mq.demo.producer;

import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 生产者 demo
 *
 * @author xlhl
 */
public class DlxDirectProducer {

    private final static String DEAD_EXCHANGE_NAME = "dlx.direct.exchange";

    private final static String EXCHANGE_NAME = "dlx.direct2.exchange";
    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.225.128");
        factory.setUsername("root");
        factory.setPassword("root");
        factory.setPort(5672);
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            //  声明死信交换机
            channel.exchangeDeclare(DEAD_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            //  声明死信队列
            //  创建队列
            String queueName1 = "leader.dlx.queue";
            channel.queueDeclare(queueName1, true, false, false, null);
            channel.queueBind(queueName1, DEAD_EXCHANGE_NAME, "leader");

            String queueName2 = "waibao.dlx.queue";
            channel.queueDeclare(queueName2, true, false, false, null);
            channel.queueBind(queueName2, DEAD_EXCHANGE_NAME, "waibao");

            DeliverCallback leaderDeliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [helena] direct_queue1 '" +
                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
            };
            DeliverCallback waiBaoDeliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [xlhl] direct_queue2 '" +
                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
                //  拒绝消息
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
            };
            channel.basicConsume(queueName1, false, leaderDeliverCallback, consumerTag -> {
            });
            channel.basicConsume(queueName2, false, waiBaoDeliverCallback, consumerTag -> {
            });

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