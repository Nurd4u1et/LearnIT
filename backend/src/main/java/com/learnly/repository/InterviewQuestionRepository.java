package com.learnly.repository;

import com.learnly.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {
    @Query(value = "SELECT * FROM interview_questions ORDER BY RANDOM() LIMIT :n", nativeQuery = true)
    List<InterviewQuestion> randomQuestions(int n);
}
