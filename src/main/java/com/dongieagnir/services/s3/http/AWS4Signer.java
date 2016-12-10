package com.dongieagnir.services.s3.http;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AWS4Signer {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private static final ZoneId UTC = ZoneId.of("UTC");
    private final Mac hmac256;

    public AWS4Signer() throws NoSuchAlgorithmException {
        hmac256 = Mac.getInstance("HmacSHA256");
    }

    public String sign(AWS4SignerContext signerContext) throws InvalidKeyException {
        String stringToSign = computeStringToSign(signerContext);
        //System.out.println(stringToSign);
        return base16Encode(hmacSha256(computeSigningKey(signerContext), getUtf8Bytes(stringToSign)));
    }

    private String computeStringToSign(AWS4SignerContext signerContext) {
        StringBuilder sb = new StringBuilder("AWS4-HMAC-SHA256\n");
        sb.append(signerContext.getDateTime().atZone(UTC).format(DATE_TIME_FORMATTER)).append('\n');
        sb.append(signerContext.getDateTime().format(DateTimeFormatter.BASIC_ISO_DATE)).append('/').append(signerContext.getRegion()).append('/').append(signerContext.getService()).append('/').append("aws4_request").append('\n');
        String canonicalRequest = createCanonicalRequest(signerContext);
        sb.append(base16Encode(hashSha256(canonicalRequest)));
        return sb.toString();
    }

    private String base16Encode(byte[] bytes) {
        return BaseEncoding.base16().encode(bytes).toLowerCase();
    }

    private byte[] hashSha256(String s) {
        HashFunction hf = Hashing.sha256();
        return hf.newHasher().putString(s, Charsets.UTF_8).hash().asBytes();
    }

    private String createCanonicalRequest(AWS4SignerContext signerContext) {
        StringBuilder sb = new StringBuilder();
        Request request = signerContext.getRequest();
        appendHttpMethod(request, sb);
        appendCanonicalURI(request, sb);
        appendCanonicalQueryString(request, sb);
        appendCanonicalHeaders(request, sb);
        appendHashedPayload(signerContext.getPayloadSignature(), sb);
        return sb.toString();
    }

    private void appendHttpMethod(Request request, StringBuilder sb) {
        sb.append(request.getMethod().name()).append('\n');
    }

    private void appendCanonicalURI(Request request, StringBuilder sb) {
        if (request.getBucket() != null && !request.getBucket().isEmpty()) {
            sb.append('/').append(request.getBucket());
        }
        sb.append('/').append(request.getKey()).append('\n');
    }

    private void appendCanonicalQueryString(Request request, StringBuilder sb) {
        if (!request.getSubResources().isEmpty()) {
            request.getSubResources().entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                    .forEach(q -> sb.append(q.getKey()).append('=').append(q.getValue()).append('&'));
            sb.setLength(sb.length() - 1);
        }
        sb.append('\n');
    }

    private void appendCanonicalHeaders(Request request, StringBuilder sb) {
        List<String> headersToSign = getHeadersToSign(request);
        if (!headersToSign.isEmpty()) {
            headersToSign.forEach(h -> sb.append(h).append(':').append(request.getHeader(h)).append('\n'));
            sb.append('\n');
            headersToSign.forEach(h -> sb.append(h).append(';'));
            sb.setLength(sb.length() - 1);
        }
        sb.append('\n');
    }

    private List<String> getHeadersToSign(Request request) {
        return request.getHeaders().keySet()
                .stream()
                .filter(this::shouldSignHeader)
                .sorted()
                .collect(Collectors.toList());
    }

    private boolean shouldSignHeader(String name) {
        return name.equals("host")
                || name.equals("range")
                || name.equals("content-type")
                || name.startsWith("x-amz-")
                ;
    }

    private void appendHashedPayload(String payloadHash, StringBuilder sb) {
        if (payloadHash != null) {
            sb.append(payloadHash);
        } else {
            sb.append("UNSIGNED-PAYLOAD");
        }
    }

    private byte[] computeSigningKey(AWS4SignerContext signerContext) throws InvalidKeyException {
        byte[] initialKey = getUtf8Bytes("AWS4" + signerContext.getCredentials().getSecretAccessKey());
        byte[] dateKey = hmacSha256(initialKey, getUtf8Bytes(signerContext.getDateTime().format(DateTimeFormatter.BASIC_ISO_DATE)));
        byte[] dateRegionKey = hmacSha256(dateKey, getUtf8Bytes(signerContext.getRegion()));
        byte[] dateRegionServiceKey = hmacSha256(dateRegionKey, getUtf8Bytes(signerContext.getService()));
        byte[] signingKey = hmacSha256(dateRegionServiceKey, getUtf8Bytes("aws4_request"));
        return signingKey;
    }

    byte[] getUtf8Bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] hmacSha256(byte[] key, byte[] text) throws InvalidKeyException {
        synchronized (hmac256) {
            hmac256.init(new SecretKeySpec(key, "HmacSHA256"));
            return hmac256.doFinal(text);
        }
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, InterruptedException {
        Request r = new Request();
        r.setMethod(Request.Method.GET);
        r.setBucket("");
        r.setKey("test.txt");
        r.addHeader("host", "examplebucket.s3.amazonaws.com");
        r.addHeader("range", "bytes=0-9");
        r.addHeader("x-amz-content-sha256", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        r.addHeader("x-amz-date", "20130524T000000Z");


        AWS4Signer signer = new AWS4Signer();
        AWS4SignerContext context = new AWS4SignerContext.Builder()
                .withRequest(r)
                .withService("s3")
                .withRegion("us-east-1")
                .withCredentials(new AWSCredentials("\tAKIAIOSFODNN7EXAMPLE", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"))
                .withDateTime(LocalDateTime.of(2013, 5, 24, 0, 0, 0))
                .withPayloadSignature("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .build();
    }
}
