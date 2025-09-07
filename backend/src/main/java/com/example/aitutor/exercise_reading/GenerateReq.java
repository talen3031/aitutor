package com.example.aitutor.exercise_reading;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GenerateReq(

    @Schema(description = "文章 ID", example = "123")
    @NotNull(message = "articleId 不可為空")
    Long articleId,

    @Schema(description = "難度（允許值：easy / medium / hard）", example = "easy", allowableValues = {"easy","medium","hard"})
    @NotNull(message = "difficulty 不可為空")
    @Pattern(regexp = "easy|medium|hard", message = "difficulty 只能是 easy / medium / hard")
    String difficulty,

    @Schema(description = "題型列表（允許值：mcq=選擇題, tf=是非題）", example = "[\"mcq\", \"tf\"]")
    @NotEmpty(message = "types 至少要有一個")
    @Size(max = 2, message = "最多只能有 2 種題型")
    List<@Pattern(regexp = "mcq|tf", message = "題型僅允許 mcq / tf") String> types,

    @Schema(description = "各題型的題目數量設定", example = "{\"mcq\":2, \"tf\":2}")
    @NotNull(message = "count 不可為空")
    Map<
        @Pattern(regexp = "mcq|tf", message = "題型 key 僅允許 mcq / tf") String,
        @NotNull(message = "數量不可為空") Integer
    > count

) {
    public GenerateReq {
        // cross-check：types 必須與 count 的 key 完全一致
        // 避免出現 types=["mcq"] 但 count={"tf":2} 這種錯誤
        if (types != null && count != null) {
            Set<String> typeSet = Set.copyOf(types);
            if (!typeSet.equals(count.keySet())) {
                throw new IllegalArgumentException("types 與 count.keySet() 必須完全一致");
            }
        }
    }
}
