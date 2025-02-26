package com.demo.MixSplit.Controller;
import com.demo.MixSplit.DTO.ApiResponse;
import com.demo.MixSplit.Service.AudioFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/database")
public class DatabaseController {

    @Autowired
    private AudioFileService audioFileService;

    public DatabaseController(AudioFileService audioFileService) {
        this.audioFileService = audioFileService;
    }

    @GetMapping("/hello/{fileName}")
    public ResponseEntity<String> hello(@PathVariable String fileName) {
        return ResponseEntity.ok("Helloword" +fileName);

    }
    @GetMapping("/audio-file-status/{fileName}")
    public ResponseEntity<ApiResponse> audioFileStatus(@PathVariable String fileName) {
        String status = audioFileService.getAudioFileStatus(fileName);
        ApiResponse response = ApiResponse.builder()
                .status("success")
                .data(Map.of(
                        "status",status
                )).build();
        return ResponseEntity.ok(response);
    }
}
