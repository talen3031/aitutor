package com.example.aitutor.exercise_listening;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListeningQuestion {

    private String question;

    private List<String> options; // 4 選項

    private Integer answer;  // 正確答案的 index (0–3)

    private String explanation;   // 答案解析
}
