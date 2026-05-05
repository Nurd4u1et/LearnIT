package com.learnly.repository;

import com.learnly.entity.InterviewAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {
    List<InterviewAnswer> findByInterviewId(Long interviewId);
}
