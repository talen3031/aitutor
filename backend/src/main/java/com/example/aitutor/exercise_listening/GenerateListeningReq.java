package com.example.aitutor.exercise_listening;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateListeningReq {

    @Schema(
        description = "難度（允許值：easy / medium / hard）",
        example = "easy",
        allowableValues = {"easy", "medium", "hard"},
        defaultValue = "easy"
    )
    @NotBlank(message = "difficulty 不能為空")
    @Pattern(regexp = "easy|medium|hard", message = "difficulty 只能是 easy / medium / hard")
    private String difficulty;

    @Schema(
        description = "題目數量（1~10）",
        example = "3",
        defaultValue = "3"
    )
    @Min(value = 1, message = "numQuestions 必須 ≥ 1")
    private int numQuestions = 3;

    @Schema(
    description = "主題列表（例如：business, daily, travel）。若空值則視為 [\"general\"]",
    example = "[\"business\", \"travel\"]",
    defaultValue = "[\"general\"]"
    )
    private List<
            @Size(max = 30, message = "topic 長度不可超過 30 字元")
            @Pattern(regexp = "^[\\p{L}0-9_-]*$", message = "topic 僅允許中英文、數字、底線與連字號")
            String
        > topics = List.of("general");

    @Schema(
        description = "文本體裁（dialogue=對話；short=小短文）",
        example = "dialogue",
        allowableValues = {"dialogue","short"},
        defaultValue = "dialogue"
    )
    @NotNull(message = "genre 不能為空")
    @Pattern(regexp = "dialogue|short", message = "genre 只能是 dialogue 或 short")
    private String genre = "dialogue";

}
