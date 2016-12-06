package com.dongieagnir.services.s3.http;

import java.util.*;

public class AWS4Signer {
    public String sign(Request request) {
        StringBuilder sb = new StringBuilder();


        appendHttpMethod(request, sb);
        appendCanonicalURI(request, sb);
        appendCanonicalQueryString(request, sb);

        return sb.toString();
    }

    private void appendHttpMethod(Request request, StringBuilder sb) {
        sb.append(request.getMethod().name()).append('\n');
    }

    private void appendCanonicalURI(Request request, StringBuilder sb) {
        sb.append('/').append(request.getBucket()).append('/').append(request.getKey()).append('\n');
    }

    private void appendCanonicalQueryString(Request request, StringBuilder sb) {
        request.getSubResources().entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(q -> sb.append(q.getKey()).append('=').append(q.getValue()).append('&'));
        sb.setLength(sb.length() - 1);
    }

    public static void main(String[] args) {
        Request r = new Request();
        r.setMethod(Request.Method.GET);
        r.setBucket("my-bucket");
        r.setKey("my-object.txt");
        r.addSubResource("versionId", "foo");

        AWS4Signer signer = new AWS4Signer();
        System.out.println(signer.sign(r));
    }
}
