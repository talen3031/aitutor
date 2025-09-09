package com.example.aitutor.submission_listening;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.example.aitutor.exercise_listening.ExerciseSetListening;
import com.example.aitutor.exercise_listening.ExerciseSetListeningRepository;
import com.example.aitutor.exercise_listening.ListeningQuestion;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * ListeningSubmissionService
 * - 從 ExerciseSetListening (items: List<ListeningQuestion>) 取題
 * - 對照使用者答案計分
 * - 保存逐題結果與總分到 ListeningSubmission
 */
@Service
@RequiredArgsConstructor
public class ListeningSubmissionService {

    private final ExerciseSetListeningRepository exerciseSetRepository;
    private final ListeningSubmissionRepository submissionRepository;

    @Transactional
    public ListeningSubmission submit(SubmitReq req) {
        // 1) 讀題組
        ExerciseSetListening set = exerciseSetRepository.findById(req.exerciseSetId())
                .orElseThrow(() -> new NoSuchElementException("ExerciseSet not found: " + req.exerciseSetId()));

        List<ListeningQuestion> items = set.getItems();
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("ExerciseSet has no items");
        }

        // 2) 整理使用者答案成 index -> answer
        Map<Integer, Object> userAnsByIndex = normalizeUserAnswers(req);

        // 3) 逐題比對
        int total = items.size();
        int correct = 0;
        List<Map<String, Object>> results = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            ListeningQuestion q = items.get(i);

            Object correctAnswer = q.getAnswer();
            Object userAnswer = userAnsByIndex.get(i);

            boolean isCorrect = compareAnswer(correctAnswer, userAnswer);
            if (isCorrect) correct++;

            Map<String, Object> perItem = new LinkedHashMap<>();
            perItem.put("index", i);
            perItem.put("question", q.getQuestion());
            perItem.put("options", q.getOptions());
            perItem.put("correct", isCorrect);
            perItem.put("correctAnswer", correctAnswer);
            perItem.put("userAnswer", userAnswer);
            perItem.put("explanation", q.getExplanation());
            results.add(perItem);
        }

        double score = total == 0 ? 0.0 : (correct * 1.0 / total);

        ListeningSubmission s = ListeningSubmission.builder()
                .exerciseSetId(set.getId())
                .answers(req.raw())   // 保存原始提交（含 exerciseSetId + responses/answers）
                .results(results)
                .total(total)
                .correct(correct)
                .score(score)
                .build();

        return submissionRepository.save(s);
    }

    /** 支援兩種提交格式，統一轉為 index -> answer */
    private static Map<Integer, Object> normalizeUserAnswers(SubmitReq req) {
        Map<Integer, Object> map = new HashMap<>();
        if (req.responses() != null && !req.responses().isEmpty()) {
            for (SubmitReq.Response r : req.responses()) {
                if (r != null && r.index() != null) {
                    map.put(r.index(), r.answer());
                }
            }
        } else if (req.answers() != null && !req.answers().isEmpty()) {
            for (int i = 0; i < req.answers().size(); i++) {
                map.put(i, req.answers().get(i));
            }
        }
        return map;
    }

    /** 答案比對邏輯：支援 int / boolean / string */
    private static boolean compareAnswer(Object correct, Object user) {
        if (correct == null || user == null) return false;

        Integer ci = toNullableInt(correct);
        Integer ui = toNullableInt(user);
        if (ci != null && ui != null) {
            return Objects.equals(ci, ui);
        }

        Boolean cb = toNullableBool(correct);
        Boolean ub = toNullableBool(user);
        if (cb != null && ub != null) {
            return Objects.equals(cb, ub);
        }

        return String.valueOf(correct).trim().equalsIgnoreCase(String.valueOf(user).trim());
    }

    private static Integer toNullableInt(Object o) {
        try {
            return Integer.valueOf(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }

    private static Boolean toNullableBool(Object o) {
        try {
            return Boolean.valueOf(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }

    // ====================== DTO ======================
    public record SubmitReq(
            Long exerciseSetId,
            List<Response> responses,   // 方案 A
            List<Object> answers,       // 方案 B
            Map<String, Object> raw     // 原始 body（原樣保存）
    ) {
        public record Response(Integer index, Object answer) {}
    }
}
