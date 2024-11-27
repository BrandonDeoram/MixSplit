package com.demo.MixSplit.Controller;
import com.demo.MixSplit.DTO.ApiResponse;
import com.demo.MixSplit.Service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/s3")
public class S3Controller {

    @Autowired
    private S3Service s3Service;


    @PostMapping("/uploadFile")
    public ResponseEntity<ApiResponse> uploadLargeFile(@RequestParam("file") MultipartFile file) throws IOException {
        File convFile = File.createTempFile("upload-", "-" + file.getOriginalFilename());
        try {
            file.transferTo(convFile);
            System.out.println("Uploading large file to S3");
            ApiResponse response = s3Service.uploadFile(convFile);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception e) {
            System.out.println("Failed to upload large file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse(500, "Failed to upload file: " + e.getMessage())
            );
        } finally {
            if (convFile.exists()) {
                convFile.delete();
            }
        }
    }


    // Download a file from S3
    @GetMapping("/download/{fileName}")
    public String downloadFile(@PathVariable String fileName) {
        System.out.println("downloading file");
        try{
            String url = s3Service.generatePresignedUrl(fileName).toString();
            return url;
        }
        catch (Exception e){
            String error = "Failed to generate presigned url: " + e.getMessage();
            return error;
        }
    }
}