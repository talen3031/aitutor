package com.example.aitutor.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aitutor.exercise.ExerciseService;
import com.example.aitutor.exercise.ExerciseSet;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController @RequestMapping("/api/exercises") @RequiredArgsConstructor
public class ExerciseController {
  private final ExerciseService service; private final ObjectMapper om;
  public record GenReq(@NotNull Long articleId,@NotBlank String difficulty,@NotNull List<String> types,@NotNull Map<String,Integer> count){}
  public record GenRes(Long exerciseSetId){}
  @PostMapping("/generate")
  public GenRes generate(@RequestBody GenReq req){ Long id=service.generateIfAbsent(req.articleId(),req.difficulty(),req.types(),req.count()); return new GenRes(id);}
  @GetMapping("/{id}")
  public Map<String,Object> get(@PathVariable Long id){ ExerciseSet set=service.get(id); return Map.of("id",set.getId(),"articleId",set.getArticle().getId(),"spec",set.getSpec(),"items",set.getItems());}
}
