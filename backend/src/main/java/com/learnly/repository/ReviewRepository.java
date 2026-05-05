package com.learnly.repository;

import com.learnly.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCourseIdOrderByCreatedAtDesc(Long courseId);
    Optional<Review> findByUserIdAndCourseId(Long userId, Long courseId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.course.id = :cid")
    Double averageForCourse(@Param("cid") Long courseId);
}
