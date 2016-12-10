package com.dongieagnir.services.s3.http;

public class AWSCredentials {
    private final String accessKeyId;
    private final String secretAccessKey;

    public AWSCredentials(String accessKeyId, String secretAccessKey) {
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }
}
