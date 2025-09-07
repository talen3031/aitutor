package com.example.aitutor.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aitutor.submission_reading.ReadingSubmission;
import com.example.aitutor.submission_reading.ReadingSubmissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/submission/reading")
@RequiredArgsConstructor
public class ReadingSubmissionController {

        private final ReadingSubmissionService submissionService;

        /**
         * 支援兩種 body（二選一）：
         * A) { "exerciseSetId": 15, "responses": [ {"index":0,"answer":0}, ... ] }
         * B) { "exerciseSetId": 15, "answers": [0,1,1,1] }
         */
        public record SubmitBody(
                @NotNull Long exerciseSetId,
                List<Map<String, Object>> responses,
                List<Object> answers
        ) {}

        @Operation(
        summary = "提交聽力題組作答",
        description = "支援兩種格式：支援兩種 body（二選一）：A) { 'exerciseSetId': 1, 'responses': [ {'index':0,'answer':0}, ... ] } B) { 'exerciseSetId': 2, 'answers': [0,1,1,1,2] }"

        )
        @PostMapping
        public Map<String, Object> submit(
                @RequestBody(
                description = "提交內容",
                required = true,
                content = @Content(
                        schema = @Schema(implementation = SubmitBody.class),
                        examples = {
                        @ExampleObject(
                                name = "Default",
                                value = """
                                {
                                "exerciseSetId": 2,
                                "answers": [0, 1, 1,3,0]
                                }
                                """
                        )
                        }
                )
                )
                @org.springframework.web.bind.annotation.RequestBody SubmitBody body
        ){
        // 原始內容也存起來（方便稽核/除錯）
        Map<String, Object> raw = new HashMap<>();
        raw.put("exerciseSetId", body.exerciseSetId());
        if (body.responses() != null) raw.put("responses", body.responses());
        if (body.answers() != null) raw.put("answers", body.answers());

        // 轉成 Service 的 SubmitReq（把 responses 轉成型別安全的 DTO）
        List<ReadingSubmissionService.SubmitReq.Response> respDtos = null;
        if (body.responses() != null) {
                respDtos = body.responses().stream()
                        .map(m -> new ReadingSubmissionService.SubmitReq.Response(
                                (Integer) m.get("index"),
                                m.get("answer")
                        ))
                        .toList();
        }

        var req = new ReadingSubmissionService.SubmitReq(
                body.exerciseSetId(),
                respDtos,               // 方案 A
                body.answers(),         // 方案 B
                raw                     // 原始 body
        );

        ReadingSubmission s = submissionService.submit(req);

        // 回傳給前端的精簡結果
        Map<String, Object> resp = new HashMap<>();
        resp.put("submissionId", s.getId());
        resp.put("exerciseSetId", s.getExerciseSetId());
        resp.put("total", s.getTotal());
        resp.put("correct", s.getCorrect());
        resp.put("score", s.getScore());
        resp.put("results", s.getResults());
        return resp;
    }
}
