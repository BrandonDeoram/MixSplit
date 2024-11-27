package com.demo.MixSplit.Rabbit;

import com.demo.MixSplit.Config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

public class RabbitMQConsumer {
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeMessage(String message) {
        System.out.println("Message received: " + message);
    }
}
