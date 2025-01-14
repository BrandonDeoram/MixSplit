package com.demo.MixSplit.Service;

import com.demo.MixSplit.Entity.Upload;
import com.demo.MixSplit.Repository.UploadRepository;
import com.demo.MixSplit.Utility.UploadStatus;
import org.springframework.stereotype.Service;

@Service
public class UploadService {

    private final UploadRepository uploadRepository;

    public UploadService(UploadRepository uploadRepository) {
        this.uploadRepository = uploadRepository;
    }

    // Adds user audio file to database
    public Upload saveUpload(Long userId, String s3Key, String fileName) {
        Upload upload = new Upload(userId,s3Key,fileName);
        return uploadRepository.save(upload);
    }
}
