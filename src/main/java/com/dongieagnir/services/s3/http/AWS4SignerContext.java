package com.dongieagnir.services.s3.http;

import java.time.LocalDateTime;

public class AWS4SignerContext {
    private final Request request;
    private final AWSCredentials credentials;
    private final String service;
    private final String region;
    private final LocalDateTime dateTime;
    private final String payloadSignature;

    public AWS4SignerContext(Request request, AWSCredentials credentials, String service, String region, LocalDateTime dateTime, String payloadSignature) {
        this.request = request;
        this.credentials = credentials;
        this.service = service;
        this.region = region;
        this.dateTime = dateTime;
        this.payloadSignature = payloadSignature;
    }

    public Request getRequest() {
        return request;
    }

    public AWSCredentials getCredentials() {
        return credentials;
    }

    public String getService() {
        return service;
    }

    public String getRegion() {
        return region;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getPayloadSignature() {
        return payloadSignature;
    }

    public static class Builder {
        private Request request;
        private AWSCredentials credentials;
        private String service;
        private String region;
        private LocalDateTime dateTime;
        private String payloadSignature;

        public Builder withRequest(Request request) {
            this.request = request;
            return this;
        }

        public Builder withCredentials(AWSCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public Builder withService(String service) {
            this.service = service;
            return this;
        }

        public Builder withRegion(String region) {
            this.region = region;
            return this;
        }

        public Builder withDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public Builder withPayloadSignature(String payloadSignature) {
            this.payloadSignature = payloadSignature;
            return this;
        }

        public AWS4SignerContext build() {
            return new AWS4SignerContext(request, credentials, service, region, dateTime, payloadSignature);
        }
    }
}
