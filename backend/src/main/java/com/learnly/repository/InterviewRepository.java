package com.learnly.repository;

import com.learnly.entity.Interview;
import com.learnly.entity.InterviewAnswer;
import com.learnly.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByUserIdOrderByStartedAtDesc(Long userId);
}
