package com.demo.MixSplit.DTO;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public class UploadFile implements Serializable {

    @NotBlank(message = "User ID is required")
    private Long userId;

    private String acrId;

    @NotBlank(message = "S3 Key is required")
    private String s3Key;
    public String fileName;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAcrId() {
        return acrId;
    }

    public void setAcrId(String acrId) {
        this.acrId = acrId;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "AudioFile{" +
                "userId=" + userId +
                ", s3Key='" + s3Key + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
