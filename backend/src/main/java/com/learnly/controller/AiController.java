package com.learnly.controller;

import com.learnly.dto.MiscDtos.*;
import com.learnly.security.CurrentUser;
import com.learnly.service.AiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService ai;
    private final CurrentUser me;

    public AiController(AiService ai, CurrentUser me) {
        this.ai = ai; this.me = me;
    }

    @GetMapping("/history")
    public List<AiMessageDto> history() {
        return ai.history(me.id()).stream().map(AiMessageDto::from).toList();
    }

    @PostMapping("/ask")
    public AiMessageDto ask(@Valid @RequestBody AiAskRequest r) {
        return AiMessageDto.from(ai.ask(me.id(), r.message(), r.lessonId()));
    }
}
