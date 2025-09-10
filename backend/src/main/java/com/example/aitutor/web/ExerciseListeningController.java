package com.example.aitutor.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.aitutor.exercise_listening.ExerciseListeningService;
import com.example.aitutor.exercise_listening.ExerciseSetListening;
import com.example.aitutor.exercise_listening.GenerateListeningReq;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/exercises/listening")
public class ExerciseListeningController {

    private final ExerciseListeningService exerciseListeningService;

    public ExerciseListeningController(ExerciseListeningService exerciseListeningService) {
        this.exerciseListeningService = exerciseListeningService;
    }

    // 取得所有聽力題組
    @GetMapping
    public List<ExerciseSetListening> getAllListeningSets() {
        return exerciseListeningService.findAll();
    }

    // 根據 id 取得特定題組
    @GetMapping("/{id}")
    public ExerciseSetListening getListeningSet(@PathVariable Long id) {
        return exerciseListeningService.findById(id);
    }

    @Operation(
        summary = "產生新的聽力題組（JSON body）",
        description = "用 JSON 提供 difficulty、numQuestions、topics、genre（dialogue/short）。"
    )
    @PostMapping("/generate")
    public ExerciseSetListening generateListeningSet(
        @RequestBody(
            description = "聽力題組生成參數",
            required = true,
            content = @Content(
                schema = @Schema(implementation = GenerateListeningReq.class),
                examples = {
                    @ExampleObject(
                        name = "Default",
                        value = """
                        {
                          "difficulty": "easy",
                          "numQuestions": 3,
                          "topics": ["business","work","boss","science"],
                          "genre": "dialogue"
                        }
                        """
                    )
                }
            )
        )
        @Valid @org.springframework.web.bind.annotation.RequestBody GenerateListeningReq req
    ) {
        // —— 參數標準化（避免 null/空白）——
        String difficulty = req.getDifficulty();
        int numQuestions = req.getNumQuestions() <= 0 ? 3 : req.getNumQuestions();
        
        List<String> topics = (req.getTopics() == null || req.getTopics().isEmpty())
            ? List.of("general")
            : req.getTopics();

        String genre = (req.getGenre() == null || req.getGenre().isBlank()) ? "dialogue" : req.getGenre();

        // —— 額外業務規則檢查（Bean Validation 之外）——
        if (numQuestions > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "numQuestions 不可超過 10");
        }

        // —— 呼叫 Service（帶 genre）——
        return exerciseListeningService.generateExercise(difficulty, numQuestions, topics, genre);
    }
}
