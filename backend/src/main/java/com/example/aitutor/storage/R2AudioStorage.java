// src/main/java/com/example/aitutor/storage/R2AudioStorage.java
package com.example.aitutor.storage;

import java.net.URI;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
public class R2AudioStorage implements AudioStorage {

    private final S3Client s3;
    private final String bucket;
    private final String publicBaseUrl; // 例如 https://cdn.example.com/audio/ （最後要有斜線）

    public R2AudioStorage(String accountId,
                          String accessKeyId,
                          String secretAccessKey,
                          String bucket,
                          String publicBaseUrl) {

        var creds = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
        var s3cfg = S3Configuration.builder()
                .pathStyleAccessEnabled(true) // R2 推薦 path-style
                .build();

        // R2 endpoint e.g. https://<accountid>.r2.cloudflarestorage.com
        this.s3 = S3Client.builder()
                .region(Region.US_EAST_1) // R2 不在 AWS，但 SDK 需要一個值，常用 us-east-1
                .credentialsProvider(creds)
                .endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
                .serviceConfiguration(s3cfg)
                .build();

        this.bucket = bucket;
        this.publicBaseUrl = ensureTrailingSlash(publicBaseUrl);
    }

    private static String ensureTrailingSlash(String s) {
        return (s.endsWith("/")) ? s : (s + "/");
    }

    @Override
    public String save(String filename, byte[] bytes) {
        String key = "audio/" + filename; // 你也可直接用根目錄，看你 publicBaseUrl 如何規劃
        var req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("audio/mpeg")
                .build();

        s3.putObject(req, RequestBody.fromBytes(bytes));
        var publicUrl = publicBaseUrl + filename; // 假設你的 publicBaseUrl 指到 audio/ 這層
        log.info("[AudioStorage:R2] uploaded s3://{}/{} -> {}", bucket, key, publicUrl);
        return publicUrl;
    }
}
