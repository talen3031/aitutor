package com.example.aitutor.storage;

public interface AudioStorage {
    /**
     * @param filename 期望的檔名（例如 "listening_xxx.mp3"）
     * @param bytes    檔案位元組
     * @return 可供前端播放的公開 URL（dev 返回 "/audio/xxx.mp3"，prod 返回 R2 公網 URL）
     */
    String save(String filename, byte[] bytes);
}
