package com.demo.MixSplit.Split;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

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
    public ResponseEntity<JsonNode> getSplit(){return splitService.getSplit();}

}
