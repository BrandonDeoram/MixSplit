package com.demo.MixSplit.Controller;
import com.demo.MixSplit.DTO.UploadFile;
import com.demo.MixSplit.Entity.AudioFile;
import com.demo.MixSplit.Rabbit.RabbitMQProducer;
import com.demo.MixSplit.Repository.AudioFileRepository;
import com.demo.MixSplit.Service.AudioFileService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/messages")
public class RabbitMQController {

    private final RabbitMQProducer producer;
    private final AudioFileRepository audioFileRepository;
    @Autowired
    public RabbitMQController(RabbitMQProducer rabbitMQProducer,AudioFileRepository audioFileRepository) {
        this.producer = rabbitMQProducer;
        this.audioFileRepository = audioFileRepository;
    }

    @PostMapping
    public String sendMessage(@Valid @RequestBody UploadFile req) {
        log.info("In send message function");
        try {

            producer.sendMessage(req);
            log.info("Message sent to queuand e for userId: {}", req.getUserId());
            return "Message sent: " + req.toString();

        } catch (Exception e) {
            log.error("Error in saving audio file to database:", e);
            throw e;
        }
    }
    // Returns all the current uploads in database
    @GetMapping
    public List<AudioFile> getAllUploads(){
        List<AudioFile> uploads = audioFileRepository.findAll();
        uploads.forEach(upload -> System.out.println("printing uploads:" +upload.toString()));
        return uploads;
    }
}