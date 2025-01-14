package com.demo.MixSplit.Rabbit;

import com.demo.MixSplit.Config.RabbitMQConfig;
import com.demo.MixSplit.DTO.UploadFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RabbitMQConsumer {
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeMessage(UploadFile file) {
        System.out.println("in consume message");
        log.info("Rabbit MQ Message Received: " + file.toString());
    }
}
