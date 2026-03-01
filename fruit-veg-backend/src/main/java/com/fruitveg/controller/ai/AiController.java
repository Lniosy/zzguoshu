package com.fruitveg.controller.ai;

import com.fruitveg.common.Result;
import com.fruitveg.service.AiClientService;
import com.fruitveg.service.MockDataService;
import com.fruitveg.utils.JwtUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiClientService aiClientService;
    private final MockDataService mockDataService;
    private final JwtUtils jwtUtils;

    public AiController(AiClientService aiClientService, MockDataService mockDataService, JwtUtils jwtUtils) {
        this.aiClientService = aiClientService;
        this.mockDataService = mockDataService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/ask")
    public Result<Map<String, Object>> ask(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        Long userId = getUserId(request);
        String question = String.valueOf(payload.getOrDefault("question", ""));
        Long sessionId = toLong(payload.get("sessionId"));
        if (sessionId == null) {
            sessionId = mockDataService.getActiveAiSessionId(userId);
        } else {
            mockDataService.switchAiSession(userId, sessionId);
        }
        List<Map<String, Object>> history = mockDataService.getAiHistoryBySession(userId, sessionId);
        String answer = aiClientService.ask(question, history);
        Map<String, Object> row = mockDataService.saveAiConversation(userId, sessionId, question, answer);
        return Result.success(row);
    }

    @GetMapping("/history")
    public Result<List<Map<String, Object>>> history(@RequestParam(value = "sessionId", required = false) Long sessionId,
                                                     HttpServletRequest request) {
        Long userId = getUserId(request);
        if (sessionId == null) {
            return Result.success(mockDataService.getAiHistory(userId));
        }
        return Result.success(mockDataService.getAiHistoryBySession(userId, sessionId));
    }

    @GetMapping("/sessions")
    public Result<List<Map<String, Object>>> sessions(HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(mockDataService.listAiSessions(userId));
    }

    @PostMapping("/sessions")
    public Result<Map<String, Object>> createSession(HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(mockDataService.createAiSession(userId));
    }

    @PutMapping("/sessions/{id}/active")
    public Result<Void> switchSession(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        return mockDataService.switchAiSession(userId, id) ? Result.success() : Result.error(404, "会话不存在");
    }

    @DeleteMapping("/sessions/{id}")
    public Result<Void> deleteSession(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        return mockDataService.deleteAiSession(userId, id) ? Result.success() : Result.error(404, "会话不存在");
    }

    @GetMapping("/faq")
    public Result<List<String>> faq() {
        return Result.success(mockDataService.getAiFaq());
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return jwtUtils.getUserIdFromToken(token.substring(7));
        }
        return 1L;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
