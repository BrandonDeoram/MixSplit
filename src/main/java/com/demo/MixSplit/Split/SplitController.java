package com.demo.MixSplit.Split;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/split")
public class SplitController {
    private final SplitService splitService;
    @Autowired
    public SplitController(SplitService splitService){
        this.splitService = splitService;
    }
    @GetMapping
    public String findSong(){
        return splitService.findSong();
    }

    @GetMapping(value = "/drake")
    public ResponseEntity<JsonNode> getSplit(){return splitService.uploadFileACRCloud();}

    @GetMapping("/upload-callback")
    public ResponseEntity<JsonNode> handleCallback() {
        return splitService.getSongs("b044f884-b5d4-42b4-9776-41a2792cb35a");
    }

}
