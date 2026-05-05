package com.learnly.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnly.entity.*;
import com.learnly.exception.ApiException;
import com.learnly.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

@Service
@Transactional
public class AiService {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String SYSTEM_PROMPT = """
        You are Learnly's coding tutor. Help the student understand their lesson and
        debug their code. Be concise (under 200 words), friendly, and pedagogical.
        Prefer guiding questions over giving away the full solution. Use Markdown
        and fenced code blocks for code.
        """;

    private final AiMessageRepository messages;
    private final UserRepository      users;
    private final LessonRepository    lessons;
    private final ObjectMapper        mapper = new ObjectMapper();
    private final HttpClient          http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();

    private final String apiKey;
    private final String model;

    public AiService(AiMessageRepository messages, UserRepository users, LessonRepository lessons,
                     @Value("${learnly.ai.anthropic-api-key}") String apiKey,
                     @Value("${learnly.ai.model}") String model) {
        this.messages = messages;
        this.users = users;
        this.lessons = lessons;
        this.apiKey = apiKey;
        this.model = model;
    }

    public List<AiMessage> history(Long userId) {
        return messages.findByUserIdOrderByCreatedAtAsc(userId);
    }

    public AiMessage ask(Long userId, String userMessage, Long lessonIdOrNull) {
        User u = users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (u.getPlan() != User.Plan.PRO) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "AI assistant requires the Pro plan");
        }

        Lesson lesson = lessonIdOrNull == null ? null
                : lessons.findById(lessonIdOrNull).orElse(null);

        // Save user message
        var userMsg = AiMessage.builder()
                .user(u).lesson(lesson)
                .role(AiMessage.Role.USER).content(userMessage)
                .build();
        messages.save(userMsg);

        // Build context: last 10 messages of THIS user
        var recent = messages.findTop20ByUserIdOrderByCreatedAtDesc(userId);
        Collections.reverse(recent);

        String reply;
        try {
            reply = callAnthropic(recent, lesson);
        } catch (Exception e) {
            reply = "AI is temporarily unavailable. (Error: "
                    + e.getClass().getSimpleName() + ")";
        }

        var aiMsg = AiMessage.builder()
                .user(u).lesson(lesson)
                .role(AiMessage.Role.ASSISTANT).content(reply)
                .build();
        return messages.save(aiMsg);
    }

    private String callAnthropic(List<AiMessage> history, Lesson lesson) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            return stubReply(history.get(history.size() - 1).getContent(), lesson);
        }

        // Build context-aware system prompt
        StringBuilder system = new StringBuilder(SYSTEM_PROMPT);
        if (lesson != null) {
            system.append("\n\nCurrent lesson: ").append(lesson.getTitle())
                  .append("\nDescription: ").append(lesson.getDescription())
                  .append("\nTask: ").append(lesson.getTask());
        }

        // Build messages array
        List<Map<String,Object>> msgs = new ArrayList<>();
        for (AiMessage m : history) {
            if (m.getRole() == AiMessage.Role.SYSTEM) continue;
            msgs.add(Map.of(
                    "role", m.getRole() == AiMessage.Role.USER ? "user" : "assistant",
                    "content", m.getContent()));
        }

        Map<String,Object> body = Map.of(
                "model", model,
                "max_tokens", 600,
                "system", system.toString(),
                "messages", msgs);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(30))
                .header("content-type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("Anthropic API HTTP " + res.statusCode() + ": " + res.body());
        }
        JsonNode root = mapper.readTree(res.body());
        StringBuilder out = new StringBuilder();
        for (JsonNode n : root.path("content")) {
            if ("text".equals(n.path("type").asText())) out.append(n.path("text").asText());
        }
        return out.toString().isBlank() ? "(empty response)" : out.toString();
    }

    private String stubReply(String userText, Lesson lesson) {
        String ctx = lesson == null ? "your studies" : "the lesson \"" + lesson.getTitle() + "\"";
        return """
               *(AI in stub mode — set ANTHROPIC_API_KEY to enable real responses.)*

               You asked: "%s"

               Here's a starting point for %s — try breaking the problem into smaller steps,
               and check the lesson description for the key concepts. If you share your code,
               I can point out where it goes wrong.
               """.formatted(userText.length() > 200 ? userText.substring(0, 200) + "..." : userText, ctx);
    }
}
