package com.demo.MixSplit.Controller;

import com.demo.MixSplit.DTO.ApiResponse;
import com.demo.MixSplit.Service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/s3")
public class S3Controller {

    @Autowired
    private S3Service s3Service;


//    @PostMapping("/uploadFile")
//    public ResponseEntity<ApiResponse> uploadLargeFile(@RequestParam("file") MultipartFile file) throws IOException {
//        File convFile = File.createTempFile("upload-", "-" + file.getOriginalFilename());
//        try {
//            file.transferTo(convFile);
//            System.out.println("Uploading large file to S3");
//            ApiResponse response = s3Service.uploadFile(convFile);
//            return ResponseEntity.status(response.getStatusCode()).body(response);
//        } catch (Exception e) {
//            System.out.println("Failed to upload large file: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    new ApiResponse(500, "Failed to upload file: " + e.getMessage())
//            );
//        } finally {
//            if (convFile.exists()) {
//                convFile.delete();
//            }
//        }
//    }

    // Generate presigned url for upload
    @GetMapping("/presigned-url")
    public ResponseEntity<ApiResponse> getPresignedUrl(@RequestParam String fileName) {
        try {
            ApiResponse response = ApiResponse.builder()
                    .status("success")
                    .data(
                            Map.of(
                                    "presigned-url", String.valueOf(s3Service.generatePresignedUrl(fileName)),
                                    "readable-url", String.valueOf(s3Service.generateReadPresignedUrl(fileName))
                            )
                    ).build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating presigned/readble url" + e.getMessage());

            ApiResponse response = ApiResponse.builder()
                    .status("error")
                    .error(
                            Map.of(
                                    "message", "Error generating presigned/readble url"
                            )
                    ).build();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }

    }

    //    Map<String, Object> response = new HashMap<>();
//
//        try {
//        // Step 1: Upload the file and start polling automatically
//        String mixId = splitService.uploadAudioACR(url, filename);
//        response.put("status", "success");
//        response.put("mixId", mixId);
//        response.put("message", "URL sent successfully and processing started");
//        return ResponseEntity.ok(response);
//
//    } catch (IOException e) {
//        // Log the error
//        System.out.println("Error uploading file: " + filename + e);
//        response.put("status", "error");
//        response.put("message", "Error uploading file: " + e.getMessage());
//        // Return an error response
//        return ResponseEntity
//                .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(response);
//    }
    @GetMapping("/read-presigned-url")
    public String getReadPresignedUrl(@RequestParam String fileName) {
        try {
            return String.valueOf(s3Service.generateReadPresignedUrl(fileName));
        } catch (Exception e) {
            String error = "Failed to generate read presigned url: " + e.getMessage();
            return error.toString();
        }

    }


    // Download a file from S3
    @GetMapping("/download/{fileName}")
    public String downloadFile(@PathVariable String fileName) {
        System.out.println("downloading file");
        try {
            String url = s3Service.generatePresignedUrl(fileName).toString();
            return url;
        } catch (Exception e) {
            String error = "Failed to generate presigned url: " + e.getMessage();
            return error;
        }
    }
}