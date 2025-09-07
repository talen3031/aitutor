package com.example.aitutor.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aitutor.exercise_reading.ExerciseReadingService;
import com.example.aitutor.exercise_reading.ExerciseSetReading;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/api/exercises/reading") 
@RequiredArgsConstructor
public class ExerciseController {
  private final ExerciseReadingService service; private final ObjectMapper om;
  public record GenReq(@NotNull Long articleId,@NotBlank String difficulty,@NotNull List<String> types,@NotNull Map<String,Integer> count){}
  public record GenRes(Long exerciseSetId){}
  
  @Operation(
        summary = "產生新的文章題組",
        description = "用 JSON 提供 articleId、difficulty、numQuestions、types、count。"
    )

  @PostMapping("/generate") 
  public GenRes generate(
    @RequestBody(
        description = "閱讀題組產生請求參數",
        required = true,
        content = @Content(
            schema = @Schema(implementation = GenReq.class),
            examples = @ExampleObject(
                name = "Default Example",
                value = """
                {
                  "articleId": 1,
                  "difficulty": "medium",
                  "types": ["mcq","tf"],
                  "count": {
                    "mcq": 5,"tf":5
                  }
                }
                """
            )
        )
    )
    @org.springframework.web.bind.annotation.RequestBody GenReq req
  ) {
    Long id = service.generateIfAbsent(req.articleId(), req.difficulty(), req.types(), req.count());
    return new GenRes(id);
  }
  @GetMapping("/{id}")
  public Map<String,Object> get(@PathVariable Long id){ ExerciseSetReading set=service.get(id); return Map.of("id",set.getId(),"articleId",set.getArticle().getId(),"spec",set.getSpec(),"items",set.getItems());}
}
