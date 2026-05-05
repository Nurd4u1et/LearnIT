package com.learnly.repository;

import com.learnly.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);
    List<LessonProgress> findByUserId(Long userId);

    @Query("""
           SELECT COUNT(lp) FROM LessonProgress lp
            WHERE lp.user.id = :userId
              AND lp.completed = TRUE
              AND lp.lesson.course.id = :courseId
           """)
    long countCompletedInCourse(@Param("userId") Long userId,
                                @Param("courseId") Long courseId);
}
