package com.demo.MixSplit.Controller;
import com.demo.MixSplit.DTO.UploadFile;
import com.demo.MixSplit.Entity.Upload;
import com.demo.MixSplit.Rabbit.RabbitMQProducer;
import com.demo.MixSplit.Repository.UploadRepository;
import com.demo.MixSplit.Service.UploadService;
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
    private final UploadService uploadService;
    private final UploadRepository uploadRepository;
    @Autowired
    public RabbitMQController(RabbitMQProducer rabbitMQProducer, UploadService uploadService, UploadRepository uploadRepository) {
        this.producer = rabbitMQProducer;
        this.uploadService = uploadService;
        this.uploadRepository = uploadRepository;
    }

    @PostMapping
    public String sendMessage(@Valid @RequestBody UploadFile req) {
        log.info("Entered");
        // Save to database
        try {
            Upload upload = uploadService.saveUpload(req.getUserId(), req.getS3Key(), req.fileName);
            log.info("Saved in database with id ", req.getUserId());

            // Send this message to Queue
            // userid, s3Key, fileName
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
    public List<Upload> getAllUploads(){
        List<Upload> uploads = uploadRepository.findAll();
        uploads.forEach(upload -> System.out.println("printing uploads:" +upload.toString()));
        return uploads;
    }
}