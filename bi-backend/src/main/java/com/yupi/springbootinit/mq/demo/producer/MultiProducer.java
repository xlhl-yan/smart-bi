package com.yupi.springbootinit.mq.demo.producer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 工作队列 producer demo
 *
 * @author xlhl
 */
public class MultiProducer {

    private static final String TASK_QUEUE_NAME = "multi_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.225.128");
        factory.setUsername("root");
        factory.setPassword("root");
        factory.setPort(5672);
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String message = scanner.nextLine();
                channel.basicPublish("", TASK_QUEUE_NAME,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + message + "'");
            }
        }
    }

}