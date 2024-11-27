package com.demo.MixSplit.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ACRConfig {
    @Value("${acrcloud.host}")
    private String host;

    @Value("${acrcloud.accessKey}")
    private String accessKey;

    @Value("${acrcloud.accessSecret}")
    private String accessSecret;

    @Value("${acrcloud.token}")
    private String token;


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getAccessSecret() {
        return accessSecret;
    }

    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }
    public String getToken() {
        return token;
    }
}
