package com.demo.MixSplit.Rabbit;

import com.demo.MixSplit.Config.RabbitMQConfig;
import com.demo.MixSplit.DTO.UploadFile;
import com.demo.MixSplit.Entity.AudioFile;
import com.demo.MixSplit.Service.AudioFileService;
import com.demo.MixSplit.Service.SplitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import java.io.IOException;

@Slf4j
@Component
public class RabbitMQConsumer {

    private final SplitService splitService;
    private final AudioFileService audioFileService;
    public RabbitMQConsumer(SplitService splitService, AudioFileService audioFileService) {
        this.splitService = splitService;
        this.audioFileService = audioFileService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME, ackMode = "MANUAL")
    public void consumeMessage(@Payload UploadFile file, Message message, Channel channel) {
        try {
            log.info("Rabbit MQ Message Received: {}", file);

            String acrId = splitService.uploadAudioACR(file.getS3Key(), file.getFileName());
            log.info("AcrId Found", acrId);

            AudioFile upload = audioFileService.saveUpload(file.getUserId(),acrId,file.getS3Key(), file.fileName);
            log.info("File saved in database");



            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); // Acknowledge success
        } catch (IOException e) {
            log.error("Error processing message: {}", e.getMessage());
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false); // Reject and do not requeue
            } catch (IOException ex) {
                log.error("Failed to nack message", ex);
            }
        }
    }
}