package com.learnly.controller;

import com.learnly.dto.MiscDtos.*;
import com.learnly.security.CurrentUser;
import com.learnly.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    private final InterviewService interview;
    private final CurrentUser      me;

    public InterviewController(InterviewService interview, CurrentUser me) {
        this.interview = interview;
        this.me = me;
    }

    @PostMapping("/start")
    public InterviewStartResponse start(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String lang) {
        return interview.start(me.id(), normalize(lang));
    }

    @PostMapping("/{id}/answer")
    public void answer(@PathVariable Long id, @Valid @RequestBody InterviewAnswerRequest r) {
        interview.saveAnswer(me.id(), id, r);
    }

    @PostMapping("/{id}/warning")
    public void warning(@PathVariable Long id, @Valid @RequestBody InterviewWarningRequest r) {
        interview.reportWarning(me.id(), id, r);
    }

    @PostMapping("/{id}/finish")
    public InterviewFinishResponse finish(@PathVariable Long id) {
        return interview.finish(me.id(), id);
    }

    private String normalize(String h) {
        return h != null && h.toLowerCase().startsWith("ru") ? "ru" : "en";
    }
}
