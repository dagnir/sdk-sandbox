package com.dongieagnir.services.s3.http;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class Request {
    public enum Method {
        GET,
        HEAD,
        PUT,
    }

    private Method method;
    private String bucket;
    private String key;
    private URI endpoint;
    private Map<String,String> subResources = new HashMap<>();
    private Map<String,String> headers = new HashMap<>();

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public URI getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(URI endpoint) {
        this.endpoint = endpoint;
    }

    public Map<String, String> getSubResources() {
        return subResources;
    }

    public void addSubResource(String name, String value) {
        subResources.put(name, value);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(String name, String value) {
        headers.put(name.toLowerCase(), value);
    }

    public String getHeader(String name) {
        return headers.get(name);
    }
}
