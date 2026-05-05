package com.learnly.repository;

import com.learnly.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseIdOrderByPositionAsc(Long courseId);
    long countByCourseId(Long courseId);
}
