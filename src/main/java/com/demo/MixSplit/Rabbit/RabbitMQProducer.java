package com.demo.MixSplit.Rabbit;

import com.demo.MixSplit.DTO.UploadFile;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.demo.MixSplit.Config.RabbitMQConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(UploadFile file) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, file);
        System.out.println("Message sent: " + file);
    }
}