package com.learnly.dto;

import com.learnly.entity.Course;
import com.learnly.entity.Lesson;

public class CourseDtos {

    public record CourseSummary(Long id, String slug, String title, String description,
                                String language, String level, String coverUrl,
                                boolean proOnly, double avgRating, long lessonCount) {

        public static CourseSummary from(Course c, String lang, double avg, long count) {
            String t = "ru".equals(lang) && c.getTitleRu() != null ? c.getTitleRu() : c.getTitle();
            String d = "ru".equals(lang) && c.getDescriptionRu() != null
                       ? c.getDescriptionRu() : c.getDescription();
            return new CourseSummary(c.getId(), c.getSlug(), t, d, c.getLanguage(),
                    c.getLevel().name(), c.getCoverUrl(), c.isProOnly(), avg, count);
        }
    }

    public record CourseDetail(CourseSummary summary, java.util.List<LessonSummary> lessons,
                               int progressPercent, boolean enrolled) {}

    public record LessonSummary(Long id, int position, String title, boolean completed) {
        public static LessonSummary from(Lesson l, String lang, boolean completed) {
            String t = "ru".equals(lang) && l.getTitleRu() != null ? l.getTitleRu() : l.getTitle();
            return new LessonSummary(l.getId(), l.getPosition(), t, completed);
        }
    }

    public record LessonDetail(Long id, Long courseId, int position,
                               String title, String description, String task,
                               String videoUrl, boolean completed) {
        public static LessonDetail from(Lesson l, String lang, boolean completed) {
            boolean ru = "ru".equals(lang);
            return new LessonDetail(
                    l.getId(),
                    l.getCourse().getId(),
                    l.getPosition(),
                    ru && l.getTitleRu()       != null ? l.getTitleRu()       : l.getTitle(),
                    ru && l.getDescriptionRu() != null ? l.getDescriptionRu() : l.getDescription(),
                    ru && l.getTaskRu()        != null ? l.getTaskRu()        : l.getTask(),
                    l.getVideoUrl(),
                    completed);
        }
    }

    // Admin create/update
    public record CreateCourseRequest(String slug, String title, String titleRu,
                                      String description, String descriptionRu,
                                      String language, String level, String coverUrl,
                                      Boolean proOnly) {}

    public record CreateLessonRequest(int position, String title, String titleRu,
                                      String videoUrl, String description, String descriptionRu,
                                      String task, String taskRu, String expectedSolution) {}
}
