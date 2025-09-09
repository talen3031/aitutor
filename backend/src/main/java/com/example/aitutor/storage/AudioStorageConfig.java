// src/main/java/com/example/aitutor/storage/AudioStorageConfig.java
package com.example.aitutor.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AudioStorageConfig {

    /**
     * 方式 A：用屬性切換
     * app.audio.storage = local | r2
     *
     * 也可改成 @Profile("dev") / @Profile("prod") 分別回傳不同 bean。
     */
    @Bean
    public AudioStorage audioStorage(
            @Value("${spring.audio.storage:local}") String mode,
            @Value("${r2.accountId:}") String accountId,
            @Value("${r2.accessKeyId:}") String accessKeyId,
            @Value("${r2.secretAccessKey:}") String secretAccessKey,
            @Value("${r2.bucket:}") String bucket,
            @Value("${r2.publicBaseUrl:}") String publicBaseUrl
    ) {
        if ("r2".equalsIgnoreCase(mode)) {
            return new R2AudioStorage(accountId, accessKeyId, secretAccessKey, bucket, publicBaseUrl);
        }
        return new LocalAudioStorage();
    }
}
