package com.demo.MixSplit.Controller;
import com.demo.MixSplit.DTO.MusicResultDTO;
import com.demo.MixSplit.Service.SplitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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

    @GetMapping("/hello")
    public String hello(){
        return "helloword";
    }

    @PostMapping(value = "/upload")
    public ResponseEntity<List<MusicResultDTO>> uploadSong(@RequestParam("file")MultipartFile file, @RequestParam("filename") String filename){
        try {
            return splitService.uploadFileACRCloud(file,filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping(value = "/uploadGetId")
    public ResponseEntity<String> uploadSongId(@RequestParam("file") MultipartFile file, @RequestParam("filename") String filename) {
        try {
            String songId = splitService.getIdSong(file, filename);
            return ResponseEntity.ok(songId);
        } catch (IOException e) {
            // Log the error
            System.out.println("Error uploading file: " + filename + e);

            // Return an error response
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }
    @PostMapping("/uploadSongPoll")
    public CompletableFuture<List<MusicResultDTO>> uploadSongPoll(@RequestParam("file") MultipartFile file, @RequestParam("filename") String filename) {
        try {
            // Step 1: Upload the file and start polling automatically
            return splitService.uploadAndPoll(file, filename);

        } catch (IOException e) {
            // Handle file upload error
            CompletableFuture<List<MusicResultDTO>> future = new CompletableFuture<>();
            future.complete((List<MusicResultDTO>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: "));
            return future;
        }
    }
    @PostMapping("/uploadMix")
    public ResponseEntity<Map<String, Object>> uploadMix(@RequestParam("file") MultipartFile file, @RequestParam("filename") String filename) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Step 1: Upload the file and start polling automatically
            String mixId = splitService.getId(file, filename);
            response.put("status", "success");
            response.put("mixId", mixId);
            response.put("message", "File uploaded successfully and processing started");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            // Log the error
            System.out.println("Error uploading file: " + filename + e);
            response.put("status", "error");
            response.put("message", "Error uploading file: " + e.getMessage());
            // Return an error response
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }
    @GetMapping("/status/{fileId}")
    public CompletableFuture<List<MusicResultDTO>> getProcessingStatus(@PathVariable String fileId) {
        return splitService.pollProcessingStatus(fileId);
    }
    @GetMapping("/checkStatus/{id}")
    public ResponseEntity<String> getStatusMix(@PathVariable String id) {
        try {
            ResponseEntity<String> response = splitService.checkProcessingStatus(id);
            return response;

        } catch (Exception e) {
            // Handle any other unexpected exceptions
            return new ResponseEntity<>("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/upload-callback")
    public ResponseEntity<List<MusicResultDTO>> handleCallback() {
        return splitService.getSongs("b044f884-b5d4-42b4-9776-41a2792cb35a");
    }
    @GetMapping("/test")
    public ResponseEntity<List<MusicResultDTO>> testFunction() {
        // Create and populate the list
        List<MusicResultDTO> data = new ArrayList<>();

        // Add MusicResultDTO objects to the list
        MusicResultDTO result1 = new MusicResultDTO("1", 101, 201, "Song One", "spotifyId1",Arrays.asList("Artist One"),"12","14");

        MusicResultDTO result2 = new MusicResultDTO("2", 102, 202, "Song Two", "spotifyId2", Arrays.asList("Artist Two"),"18","20");

        data.add(result1);
        data.add(result2);

        // Return the list as JSON in the response
        return ResponseEntity.status(HttpStatus.OK).body(data);
    }


}


