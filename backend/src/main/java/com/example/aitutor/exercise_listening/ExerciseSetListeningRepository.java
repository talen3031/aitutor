package com.example.aitutor.exercise_listening;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseSetListeningRepository extends JpaRepository<ExerciseSetListening, Long> {
    
}
