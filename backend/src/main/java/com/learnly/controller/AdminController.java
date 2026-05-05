package com.learnly.controller;

import com.learnly.dto.CourseDtos.*;
import com.learnly.entity.Course;
import com.learnly.entity.Lesson;
import com.learnly.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService admin;
    public AdminController(AdminService admin) { this.admin = admin; }

    @PostMapping("/courses")
    public Course createCourse(@RequestBody CreateCourseRequest r) {
        return admin.createCourse(r);
    }

    @PostMapping("/courses/{id}/lessons")
    public Lesson addLesson(@PathVariable Long id, @RequestBody CreateLessonRequest r) {
        return admin.addLesson(id, r);
    }

    @PostMapping("/comments/{id}/hide")
    public void hideComment(@PathVariable Long id) { admin.hideComment(id); }

    @GetMapping("/analytics")
    public Map<String, Object> analytics() { return admin.analytics(); }
}
