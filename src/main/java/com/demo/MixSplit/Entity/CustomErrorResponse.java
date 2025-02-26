package com.demo.MixSplit.Entity;

public class CustomErrorResponse {
    private String errorCode;
    private String message;
    private int status;

    // Constructors, getters, and setters
    public CustomErrorResponse() {}

    public CustomErrorResponse(String errorCode, String message, int status) {
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
    }

    // Getters and setters for all fields
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}