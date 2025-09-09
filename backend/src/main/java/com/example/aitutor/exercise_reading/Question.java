package com.example.aitutor.exercise_reading;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Question {
  private String type;
  private String prompt;
  private List<String> options;
  private Object answer;
  private String explanation;
}
