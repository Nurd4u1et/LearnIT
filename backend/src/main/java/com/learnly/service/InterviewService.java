package com.learnly.service;

import com.learnly.dto.MiscDtos.*;
import com.learnly.entity.*;
import com.learnly.exception.ApiException;
import com.learnly.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class InterviewService {

    public static final int QUESTION_COUNT = 3;
    public static final int DURATION_MIN   = 15;
    public static final int MAX_WARNINGS   = 3;

    private final InterviewRepository         interviews;
    private final InterviewQuestionRepository questions;
    private final InterviewAnswerRepository   answers;
    private final UserRepository              users;

    public InterviewService(InterviewRepository interviews,
                            InterviewQuestionRepository questions,
                            InterviewAnswerRepository answers,
                            UserRepository users) {
        this.interviews = interviews;
        this.questions = questions;
        this.answers = answers;
        this.users = users;
    }

    public InterviewStartResponse start(Long userId, String lang) {
        var qs = questions.randomQuestions(QUESTION_COUNT);
        if (qs.size() < QUESTION_COUNT)
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Not enough interview questions in DB");

        Interview iv = Interview.builder()
                .user(users.getReferenceById(userId))
                .build();
        interviews.save(iv);

        for (var q : qs) {
            answers.save(InterviewAnswer.builder()
                    .interview(iv).question(q).answer("").build());
        }

        var dtos = qs.stream().map(q -> InterviewQuestionDto.from(q, lang)).toList();
        return new InterviewStartResponse(iv.getId(), dtos, DURATION_MIN);
    }

    public void saveAnswer(Long userId, Long interviewId, InterviewAnswerRequest req) {
        Interview iv = ensureOwned(userId, interviewId);
        if (iv.getStatus() != Interview.Status.IN_PROGRESS)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Interview is not in progress");
        var ans = answers.findByInterviewId(interviewId).stream()
                .filter(a -> a.getQuestion().getId().equals(req.questionId()))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not in interview"));
        ans.setAnswer(req.answer() == null ? "" : req.answer());
    }

    public Interview reportWarning(Long userId, Long interviewId, InterviewWarningRequest req) {
        Interview iv = ensureOwned(userId, interviewId);
        iv.setWarnings(iv.getWarnings() + 1);
        if (iv.getWarnings() >= MAX_WARNINGS) {
            iv.setStatus(Interview.Status.TERMINATED);
            iv.setFinishedAt(OffsetDateTime.now());
            iv.setScore(0);
        }
        return iv;
    }

    public InterviewFinishResponse finish(Long userId, Long interviewId) {
        Interview iv = ensureOwned(userId, interviewId);
        if (iv.getStatus() == Interview.Status.IN_PROGRESS) {
            List<InterviewAnswer> as = answers.findByInterviewId(interviewId);
            int correct = 0;
            for (var a : as) {
                boolean ok = grade(a.getAnswer(), a.getQuestion());
                a.setCorrect(ok);
                if (ok) correct++;
            }
            int score = (int) Math.round(100.0 * correct / Math.max(1, as.size()));
            // Penalize for warnings
            score = Math.max(0, score - (iv.getWarnings() * 10));
            iv.setScore(score);
            iv.setFinishedAt(OffsetDateTime.now());
            iv.setDurationSec((int) ChronoUnit.SECONDS
                    .between(iv.getStartedAt(), iv.getFinishedAt()));
            iv.setStatus(Interview.Status.FINISHED);
        }
        return new InterviewFinishResponse(iv.getId(),
                iv.getScore() == null ? 0 : iv.getScore(),
                QUESTION_COUNT, iv.getWarnings(), iv.getStatus().name());
    }

    public List<Interview> history(Long userId) {
        return interviews.findByUserIdOrderByStartedAtDesc(userId);
    }

    private Interview ensureOwned(Long userId, Long interviewId) {
        Interview iv = interviews.findById(interviewId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Interview not found"));
        if (!iv.getUser().getId().equals(userId))
            throw new ApiException(HttpStatus.FORBIDDEN, "Not your interview");
        return iv;
    }

    /** Heuristic grader: looks for keyword(s) typical of a correct solution. */
    private boolean grade(String code, InterviewQuestion q) {
        if (code == null || code.isBlank()) return false;
        String c = code.toLowerCase();
        String title = q.getTitle().toLowerCase();
        if (title.contains("reverse"))   return c.contains("[::-1]") || c.contains("reverse");
        if (title.contains("fizzbuzz"))  return c.contains("fizz") && c.contains("buzz");
        if (title.contains("sum"))       return c.contains("sum") || c.contains("+=");
        if (title.contains("palindrome"))return c.contains("equals") || c.contains("==")
                                                || c.contains("reverse");
        if (title.contains("vowel"))     return c.contains("aeiou") || c.contains("vowel");
        // Fallback: non-trivial answer counts as attempt-correct
        return c.length() > 30;
    }
}
