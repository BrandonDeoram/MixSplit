package com.demo.MixSplit.Service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.demo.MixSplit.DTO.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class S3Service {
    private final AmazonS3 amazonS3;
    private static final long MULTIPART_THRESHOLD = 5 * 1024 * 1024; // 5 MB
    @Value("${aws.s3.bucket}")

    private String bucketName;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }
    // Upload file to S3 bucket - Smaller files 100MB

//    public ApiResponse uploadFile(File file) {
//        if (file.length() > MULTIPART_THRESHOLD){
//            uploadLargeFile(file);
//        }
//        else{
//            try {
//                amazonS3.putObject(new PutObjectRequest(bucketName, file.getName(), file));
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                return new ApiResponse(500,"Error uploading file: " + e.getMessage());
//            }
//        }
//        return new ApiResponse(200, "File uploaded successfully");
//
//    }
    public S3Object downloadFile(String fileName) {
        return amazonS3.getObject(bucketName, fileName);
    }

    public URL generatePresignedUrl(String fileName){
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 60);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, fileName)
                .withMethod(HttpMethod.PUT)
                .withExpiration(expiration)
                .withContentType("audio/mpeg");
        return this.amazonS3.generatePresignedUrl(request);
    }
    public URL generateReadPresignedUrl(String fileName) {
        try {
            // Set expiration time to 1 hour from now
            Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 60);

            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucketName, fileName)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);

            return amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        } catch (AmazonS3Exception e) {
            throw new RuntimeException("Failed to generate read presigned URL", e);
        }
    }
    private void uploadLargeFile(File file) {
        // Initiate the multipart upload
        // Telling AWS were going to give you a big file, please give us a uploadId
        String userId = "312";
        String key = userId + file.getName();
        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, key);
        InitiateMultipartUploadResult initResponse = this.amazonS3.initiateMultipartUpload(initRequest);

        try {
            // Track uploaded parts, almost like a checklist
            List<PartETag> partETags = new ArrayList<>();

            // Upload file in parts
            long fileSize = file.length();
            long bytesRead = 0;
            int partNumber = 1;

            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                while (bytesRead < fileSize) {
                    // Determine part size, breaking it up
                    long remainingBytes = fileSize - bytesRead;
                    long currentPartSize = Math.min(MULTIPART_THRESHOLD, remainingBytes);

                    // Prepare upload request for this part
                    UploadPartRequest uploadRequest = new UploadPartRequest()
                            .withBucketName(bucketName)
                            .withKey(key)
                            .withUploadId(initResponse.getUploadId())
                            .withPartNumber(partNumber)
                            .withPartSize(currentPartSize)
                            .withInputStream(fileInputStream);

                    // Upload the part and store its ETag
                    UploadPartResult uploadResult = this.amazonS3.uploadPart(uploadRequest);
                    partETags.add(uploadResult.getPartETag());

                    bytesRead += currentPartSize;
                    partNumber++;
                }
            }

            // Complete multipart upload, telling aws im done sending all the pieces
            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(
                    bucketName,
                    key,
                    initResponse.getUploadId(),
                    partETags
            );
            this.amazonS3.completeMultipartUpload(compRequest);

        } catch (Exception e) {
            // Abort upload if something goes wrong
            this.amazonS3.abortMultipartUpload(
                    new AbortMultipartUploadRequest(bucketName, key, initResponse.getUploadId())
            );
            throw new RuntimeException("Multipart upload failed", e);
        }
    }
}

