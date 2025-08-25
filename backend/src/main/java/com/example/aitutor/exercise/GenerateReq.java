package com.example.aitutor.exercise;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotNull;

/**
 * 生題請求 DTO（從 Controller 抽離成獨立檔案，避免 Service 依賴 Controller 內部型別）
 */
public record GenerateReq(
    @NotNull Long articleId,
    @NotNull String difficulty,                 // 例：B1
    @NotNull List<String> types,                // 例：["mcq","tf"]
    @NotNull Map<String, Integer> count         // 例：{"mcq":2,"tf":2}
) {}
