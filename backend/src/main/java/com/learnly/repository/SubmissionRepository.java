package com.learnly.repository;

import com.learnly.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByUserIdAndLessonIdOrderByCreatedAtDesc(Long userId, Long lessonId);
}
