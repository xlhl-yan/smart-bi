package com.yupi.springbootinit.mq.demo.consumer;

import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 消费者 demo
 *
 * @author xlhl
 */
public class DlxDirectConsumer {

    private final static String DEAD_EXCHANGE_NAME = "dlx.direct.exchange";

    private final static String EXCHANGE_NAME = "dlx.direct2.exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.225.128");
        factory.setUsername("root");
        factory.setPassword("root");
        factory.setPort(5672);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare("some.exchange.name", "direct");

        Map<String, Object> dogArgs = new HashMap<>(16);
        dogArgs.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        dogArgs.put("x-dead-letter-routing-key", "waibao");
        dogArgs.put("x-message-ttl", 1000 * 5);

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        //  创建队列    绑定到 waibao
        String queueName1 = "dog";
        channel.queueDeclare(queueName1, true, false, false, dogArgs);
        channel.queueBind(queueName1, EXCHANGE_NAME, "dog");

        //  绑定到 leader
        Map<String, Object> catArgs = new HashMap<>(16);
        //  绑定死信交换机
        catArgs.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        //  发送到死信交换机时使用的 RoutingKey
        catArgs.put("x-dead-letter-routing-key", "leader");
        catArgs.put("x-message-ttl", 1000 * 5);
        String queueName2 = "cat";
        channel.queueDeclare(queueName2, true, false, false, catArgs);
        channel.queueBind(queueName2, EXCHANGE_NAME, "cat");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [helena] direct_queue1 '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
        };
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [xlhl] direct_queue2 '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
            //  拒绝消息
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
        };
        channel.basicConsume(queueName1, false, deliverCallback1, consumerTag -> {
        });
        channel.basicConsume(queueName2, false, deliverCallback2, consumerTag -> {
        });


    }
}