package com.demo.MixSplit.Utility;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class ACRHeaders {

    // Create headers with Bearer Authentication
    public static HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set("Content-Type", "application/x-www-form-urlencoded");
        return headers;
    }

    // Create headers for multipart form data
    public static HttpHeaders createMultipartHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }
}
