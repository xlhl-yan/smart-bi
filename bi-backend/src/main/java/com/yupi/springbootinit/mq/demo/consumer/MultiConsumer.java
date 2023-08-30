package com.yupi.springbootinit.mq.demo.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

/**
 * 工作队列 consumer demo
 *
 * @author xlhl
 */
public class MultiConsumer {

    private static final String TASK_QUEUE_NAME = "multi_queue";

    public static void main(String[] argv) throws Exception {
        //  创建工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.225.128");
        factory.setUsername("root");
        factory.setPassword("root");
        factory.setPort(5672);
        //  建立连接
        final Connection connection = factory.newConnection();
        for (int i = 0; i < 2; i++) {
            final Channel channel = connection.createChannel();

            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            channel.basicQos(1);

            //  定义了如何处理消息
            int finalI = i;
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

                try {
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    System.out.println(" [x] Received ' 编号：" + finalI + message + "'");
                    //  处理工作的逻辑
                    //  停 20s 模拟每个机器处理能力有限
                    Thread.sleep(1000 * 20);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                } finally {
                    System.out.println(" [x] Done");
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            //  开启消费监听
            channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> {
            });
        }
    }
}