package com.demo.MixSplit.Split;
import com.demo.MixSplit.DTO.MusicResultDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "api/v1/split")
public class SplitController {
    private final SplitService splitService;
    @Autowired
    public SplitController(SplitService splitService){
        this.splitService = splitService;
    }

    @PostMapping("/findSong")
    public String findSong(){
        return splitService.findSong();
    }

    @PostMapping(value = "/upload")
    public ResponseEntity<List<MusicResultDTO>> uploadSong(@RequestParam("file")MultipartFile file, @RequestParam("filename") String filename){
        try {
            return splitService.uploadFileACRCloud(file,filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/upload-callback")
    public ResponseEntity<List<MusicResultDTO>> handleCallback() {
        return splitService.getSongs("b044f884-b5d4-42b4-9776-41a2792cb35a");
    }

}
