package com.example.aitutor.submission_listening;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ListeningSubmissionRepository extends JpaRepository<ListeningSubmission, Long> {
    List<ListeningSubmission> findByExerciseSetId(Long exerciseSetId);
}
