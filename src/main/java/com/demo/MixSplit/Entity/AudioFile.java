package com.demo.MixSplit.Entity;
import com.demo.MixSplit.Utility.UploadStatus;
import jakarta.persistence.*;

@Entity
public class AudioFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name ="user_id",nullable = false)
    private Long userId;

    @Column(name ="acr_id",nullable = true)
    private String acrId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String s3Key;

    @Column(name = "file_name",nullable = false)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private UploadStatus status = UploadStatus.PENDING;

    public AudioFile(Long userId, String acrId, String s3Key, String fileName) {
        this.userId = userId;
        this.acrId = acrId;
        this.s3Key = s3Key;
        this.fileName = fileName;
        this.status = status;
    }

    public AudioFile() {
    }


    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public UploadStatus getStatus() {
        return status;
    }

    public void setStatus(UploadStatus status) {
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
