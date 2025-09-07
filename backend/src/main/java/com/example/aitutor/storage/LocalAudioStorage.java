// src/main/java/com/example/aitutor/storage/LocalAudioStorage.java
package com.example.aitutor.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalAudioStorage implements AudioStorage {

    private final Path srcDir;
    private final Path targetDir;

    public LocalAudioStorage() {
        this.srcDir = Paths.get("src/main/resources/static/audio");
        this.targetDir = Paths.get("target/classes/static/audio");
    }

    @Override
    public String save(String filename, byte[] bytes) {
        try {
            Files.createDirectories(srcDir);
            Files.createDirectories(targetDir);

            Path srcOut = srcDir.resolve(filename);
            Path tgtOut = targetDir.resolve(filename);

            Files.write(srcOut, bytes);
            Files.write(tgtOut, bytes);

            log.info("[AudioStorage:local] wrote {} and {}", srcOut.toAbsolutePath(), tgtOut.toAbsolutePath());
            // 前端可直接以 /audio/xxx.mp3 取用（由 Spring 靜態資源處理）
            return "/audio/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write local audio file", e);
        }
    }
}
