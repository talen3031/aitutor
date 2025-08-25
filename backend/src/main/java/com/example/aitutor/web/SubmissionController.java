package com.example.aitutor.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aitutor.submission.Submission;
import com.example.aitutor.submission.SubmissionService;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/submission")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

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

    @PostMapping
    public Map<String, Object> submit(@RequestBody SubmitBody body) {
        // 原始內容也存起來（方便稽核/除錯）
        Map<String, Object> raw = new HashMap<>();
        raw.put("exerciseSetId", body.exerciseSetId());
        if (body.responses() != null) raw.put("responses", body.responses());
        if (body.answers() != null) raw.put("answers", body.answers());

        // 轉成 Service 的 SubmitReq（把 responses 轉成型別安全的 DTO）
        List<SubmissionService.SubmitReq.Response> respDtos = null;
        if (body.responses() != null) {
            respDtos = body.responses().stream()
                    .map(m -> new SubmissionService.SubmitReq.Response(
                            (Integer) m.get("index"),
                            m.get("answer")
                    ))
                    .toList();
        }

        var req = new SubmissionService.SubmitReq(
                body.exerciseSetId(),
                respDtos,               // 方案 A
                body.answers(),         // 方案 B
                raw                     // 原始 body
        );

        Submission s = submissionService.submit(req);

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
