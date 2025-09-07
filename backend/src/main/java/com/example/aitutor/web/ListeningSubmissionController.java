package com.example.aitutor.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aitutor.submission_listening.ListeningSubmission;
import com.example.aitutor.submission_listening.ListeningSubmissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/submissions/listening")
@RequiredArgsConstructor
public class ListeningSubmissionController {

    private final ListeningSubmissionService service;

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
        description = "支援兩種格式：支援兩種 body（二選一）：A) { 'exerciseSetId': 2, 'responses': [ {'index':0,'answer':0}, ... ] } B) { 'exerciseSetId': 2, 'answers': [0,1,1] }"
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
                          "answers": [0, 1, 1]
                        }
                        """
                    )
                }
            )
        )
        @org.springframework.web.bind.annotation.RequestBody SubmitBody body
    )
    {
        // 原始內容存起來
        Map<String, Object> raw = new HashMap<>();
        raw.put("exerciseSetId", body.exerciseSetId());
        if (body.responses() != null) raw.put("responses", body.responses());
        if (body.answers() != null) raw.put("answers", body.answers());

        // 轉成 Service 的 SubmitReq
        List<ListeningSubmissionService.SubmitReq.Response> respDtos = null;
        if (body.responses() != null) {
            respDtos = body.responses().stream()
                    .map(m -> new ListeningSubmissionService.SubmitReq.Response(
                            (Integer) m.get("index"),
                            m.get("answer")
                    ))
                    .toList();
        }

        var req = new ListeningSubmissionService.SubmitReq(
                body.exerciseSetId(),
                respDtos,       // 方案 A
                body.answers(), // 方案 B
                raw             // 原始 body
        );

        ListeningSubmission s = service.submit(req);

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
