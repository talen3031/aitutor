package com.example.aitutor.submission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap; // <<— 重要：改用 Question 類別
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.example.aitutor.exercise.ExerciseSet;
import com.example.aitutor.exercise.ExerciseSetRepository;
import com.example.aitutor.exercise.Question;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * SubmissionService
 * - 從 ExerciseSet (items: List<Question>) 取題
 * - 對照使用者答案計分
 * - 保存逐題結果與總分到 Submission
 *
 * 支援兩種提交格式（二選一）：
 *  A) responses = [{index:0, answer:1}, ...]
 *  B) answers   = [1,0,2,3]  // 依題目順序
 *
 * 會把原始提交（raw）原封不動存進 Submission.answers 便於稽核/除錯。
 */
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final ExerciseSetRepository exerciseSetRepository;
    private final SubmissionRepository submissionRepository;

    @Transactional
    public Submission submit(SubmitReq req) {
        // 1) 讀題組
        ExerciseSet set = exerciseSetRepository.findById(req.exerciseSetId())
                .orElseThrow(() -> new NoSuchElementException("ExerciseSet not found: " + req.exerciseSetId()));

        // ===== 這裡改成 List<Question>，修正你的編譯錯誤 =====
        List<Question> items = set.getItems();
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
            Question q = items.get(i);

            Object correctAnswer = q.getAnswer();          // 依你的 Question 設計，常見為 Integer/Boolean/String
            Object userAnswer    = userAnsByIndex.get(i);

            boolean isCorrect = compareAnswer(correctAnswer, userAnswer);
            if (isCorrect) correct++;

            Map<String, Object> perItem = new LinkedHashMap<>();
            perItem.put("index", i);
            perItem.put("type", q.getType());
            perItem.put("prompt", q.getPrompt());
            perItem.put("options", q.getOptions());        // 若非 MCQ 可能為 null，保留給前端參考
            perItem.put("correct", isCorrect);
            perItem.put("correctAnswer", correctAnswer);
            perItem.put("userAnswer", userAnswer);
            perItem.put("explanation", q.getExplanation());
            results.add(perItem);
        }

        double score = total == 0 ? 0.0 : (correct * 1.0 / total);

        Submission s = Submission.builder()
                .exerciseSetId(set.getId())
                .answers(req.raw())   // 保存原始提交（含 exerciseSetId + responses/answers）
                .results(results)
                .total(total)
                .correct(correct)
                .score(score)
                .build();

        return submissionRepository.save(s);
    }

    /**
     * 支援兩種提交格式，統一轉為 index -> answer
     */
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

    /**
     * 寬鬆比對：
     * 1) 兩者皆可解析為整數 -> 整數相等（MCQ 常見）
     * 2) 兩者皆可解析為布林 -> 布林相等（TF）
     * 3) 其他 -> 轉字串忽略大小寫與前後空白
     */
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

        String cs = String.valueOf(correct).trim();
        String us = String.valueOf(user).trim();
        System.out.println("DEBUG correct=" + correct + " (" + correct.getClass() + "), user=" + user + " (" + user.getClass() + ")");
        return cs.equalsIgnoreCase(us);
    }

    private static Integer toNullableInt(Object o) {
        try {
            if (o instanceof Number n) return n.intValue();
            String s = String.valueOf(o).trim();
            if (s.matches("[-+]?\\d+")) return Integer.parseInt(s);
        } catch (Exception ignored) {}
        return null;
    }

    private static Boolean toNullableBool(Object o) {
        if (o instanceof Boolean b) return b;
        String s = String.valueOf(o).trim().toLowerCase(Locale.ROOT);
        return switch (s) {
            case "true", "t", "1", "yes", "y" -> true;
            case "false", "f", "0", "no", "n" -> false;
            default -> null;
        };

    }

    // ====================== DTO ======================

    /**
     * Service 層提交請求 DTO
     */
    public record SubmitReq(
            Long exerciseSetId,
            List<Response> responses,   // 方案 A
            List<Object> answers,       // 方案 B
            Map<String, Object> raw     // 原始 body（原樣保存）
    ) {
        public record Response(Integer index, Object answer) {}
    }
}
