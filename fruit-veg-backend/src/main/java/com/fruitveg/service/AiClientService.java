package com.fruitveg.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AiClientService {

    private static final Logger logger = LoggerFactory.getLogger(AiClientService.class);
    private static final String DEFAULT_MODEL = "deepseek-ai/DeepSeek-V3";
    private static final String DEFAULT_SYSTEM_PROMPT =
            "你是郑州市果蔬销售平台的AI果蔬专家。仅回答果蔬相关问题：营养、保鲜、食用、选购。"
                    + "必须结合当前对话上下文连续回答，不要忽略用户上一轮的条件。"
                    + "回答要求：简洁、可执行、中文。对于危险饮食建议要提醒谨慎。"
                    + "如果问题明显偏离果蔬主题，先简短提示主题范围，再给出可咨询的果蔬方向。";

    private volatile String apiKey;
    private volatile String apiBaseUrl;
    private volatile String modelName = DEFAULT_MODEL;
    private volatile long lastModified = -1L;
    // SiliconFlow chat/completions 文档标注 messages 最多 10 个元素。
    // 这里预留 system + 当前 user，共 2 个，历史上下文最多放 4 轮（8 个消息）。
    private static final int CONTEXT_ROUND_LIMIT = 4;
    private static final int CONTENT_MAX_LEN = 500;

    @PostConstruct
    public void init() {
        reloadConfigIfNeeded();
    }

    public String ask(String question) {
        return ask(question, Collections.emptyList());
    }

    public String ask(String question, List<Map<String, Object>> history) {
        if (question == null || question.trim().isEmpty()) {
            return "请先输入你想咨询的果蔬问题。";
        }
        reloadConfigIfNeeded();
        if (apiKey == null || apiKey.isEmpty() || apiBaseUrl == null || apiBaseUrl.isEmpty()) {
            return "AI服务暂未配置完成，请稍后再试。";
        }

        try {
            JSONObject payload = new JSONObject();
            payload.set("model", modelName == null || modelName.isEmpty() ? DEFAULT_MODEL : modelName);
            payload.set("temperature", 0.5);
            payload.set("max_tokens", 512);

            JSONArray messages = new JSONArray();
            messages.add(new JSONObject().set("role", "system").set("content", DEFAULT_SYSTEM_PROMPT));
            appendContextMessages(messages, history);
            messages.add(new JSONObject().set("role", "user").set("content", question.trim()));
            payload.set("messages", messages);

            HttpResponse response = HttpRequest.post(apiBaseUrl)
                    .timeout(30000)
                    .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                    .header(Header.CONTENT_TYPE, "application/json")
                    .body(payload.toString())
                    .execute();

            int status = response.getStatus();
            String body = response.body();
            if (status < 200 || status >= 300) {
                logger.warn("AI request failed, status={}, body={}", status, body);
                return "AI服务当前繁忙，请稍后重试。";
            }
            JSONObject json = JSONUtil.parseObj(body);
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                logger.warn("AI response has no choices: {}", body);
                return "AI暂时没有生成有效回答，请换个问法再试。";
            }
            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            String content = message == null ? null : message.getStr("content");
            if (content == null || content.trim().isEmpty()) {
                return "AI暂时没有生成有效回答，请换个问法再试。";
            }
            return content.trim();
        } catch (Exception e) {
            logger.error("AI request exception: {}", e.getMessage(), e);
            return "AI服务连接异常，请稍后重试。";
        }
    }

    private void appendContextMessages(JSONArray messages, List<Map<String, Object>> history) {
        if (history == null || history.isEmpty()) {
            return;
        }
        int start = Math.max(0, history.size() - CONTEXT_ROUND_LIMIT);
        for (int i = start; i < history.size(); i++) {
            Map<String, Object> row = history.get(i);
            if (row == null) {
                continue;
            }
            String question = safeText(row.get("question"));
            String answer = safeText(row.get("answer"));
            if (!question.isEmpty()) {
                messages.add(new JSONObject().set("role", "user").set("content", question));
            }
            if (!answer.isEmpty()) {
                messages.add(new JSONObject().set("role", "assistant").set("content", answer));
            }
        }
    }

    private String safeText(Object raw) {
        if (raw == null) {
            return "";
        }
        String text = String.valueOf(raw).trim();
        if (text.isEmpty()) {
            return "";
        }
        return text.length() > CONTENT_MAX_LEN ? text.substring(0, CONTENT_MAX_LEN) : text;
    }

    private synchronized void reloadConfigIfNeeded() {
        File file = resolveConfigFile();
        if (file == null || !file.exists()) {
            apiKey = null;
            apiBaseUrl = null;
            return;
        }
        long modified = file.lastModified();
        if (modified == lastModified && apiKey != null && apiBaseUrl != null) {
            return;
        }
        List<String> lines = FileUtil.readLines(file, StandardCharsets.UTF_8);
        String key = "";
        String url = "";
        String model = "";
        for (String raw : lines) {
            String line = raw == null ? "" : raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int idx = line.indexOf(':');
            if (idx <= 0) {
                continue;
            }
            String k = line.substring(0, idx).trim();
            String v = line.substring(idx + 1).trim();
            if ("API_KEY".equalsIgnoreCase(k)) {
                key = v;
            } else if ("API_BASE_URL".equalsIgnoreCase(k)) {
                url = v;
            } else if ("API_MODEL".equalsIgnoreCase(k)) {
                model = v;
            }
        }
        apiKey = key;
        apiBaseUrl = url;
        modelName = model == null || model.trim().isEmpty() ? DEFAULT_MODEL : model.trim();
        lastModified = modified;
        logger.info("AI config loaded from {}, baseUrl={}, model={}, keyPresent={}",
                file.getAbsolutePath(), apiBaseUrl, modelName, apiKey != null && !apiKey.isEmpty());
    }

    private File resolveConfigFile() {
        List<String> candidates = new ArrayList<>();
        String envPath = System.getenv("FRUITVEG_AI_CONFIG_PATH");
        if (envPath != null && !envPath.trim().isEmpty()) {
            candidates.add(envPath.trim());
        }
        candidates.add("config/ai-config.txt");
        candidates.add("../config/ai-config.txt");
        for (String p : candidates) {
            File f = new File(p);
            if (f.exists() && f.isFile()) {
                return f;
            }
        }
        return null;
    }
}
