package com.multi.travel.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : GeminiClient
 * @since : 2025-11-10 월요일
 */
@Slf4j
@Component
public class GeminiClient {

    /** .env 또는 application.yml에서 주입 */
    @Value("${gemini.api.key}")
    private String apiKey;

    /** Gemini Flash 모델 엔드포인트 */
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Gemini API 호출
     *
     * @param prompt 프롬프트(요청할 문장)
     * @return 모델의 응답 텍스트(JSON 문자열)
     */
    public String generate(String prompt) {
        try {
            String body = """
                {
                  "contents": [
                    {
                      "parts": [
                        { "text": "%s" }
                      ]
                    }
                  ]
                }
            """.formatted(escapeJson(prompt));

            // 2. HTTP 요청 구성
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            // 3. API 호출
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Gemini API 호출 실패 - 상태코드: {}", response.statusCode());
                log.error("응답 본문: {}", response.body());
                throw new RuntimeException("Gemini API 호출 실패: " + response.statusCode());
            }

            // 1) parts 전체 병합
            String text = extractText(response.body());

            // 2) 불필요한 코드블록 제거
            String cleaned = cleanResponse(text);

            // 3) JSON만 추출 (제일 중요)
            String json = extractJsonOnly(cleaned);

            log.info("Gemini 응답 JSON 추출 완료");

            return json;

        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생", e);
            throw new RuntimeException("Gemini API 호출 실패", e);
        }
    }

    /** parts 전체 병합 */
    private String extractText(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode parts = root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts");

            if (!parts.isArray() || parts.isEmpty()) {
                throw new RuntimeException("Gemini 응답에 parts가 없습니다: " + json);
            }

            StringBuilder sb = new StringBuilder();
            for (JsonNode part : parts) {
                if (part.has("text")) {
                    sb.append(part.get("text").asText());
                }
            }

            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("Gemini 응답 파싱 실패", e);
        }
    }

    /** 불필요한 ``` 제거 (개행 유지) */
    private String cleanResponse(String text) {
        if (text == null) return "";
        return text
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }

    /** JSON만 추출 (설명 문장 제거 핵심 로직) */
    public static String extractJsonOnly(String raw) {

        if (raw == null || raw.isBlank()) {
            throw new RuntimeException("AI 응답이 비어 있습니다.");
        }

        // 1) 응답에서 처음 등장하는 '{' 위치
        int start = raw.indexOf("{");
        // 2) 응답에서 마지막 등장하는 '}' 위치
        int end = raw.lastIndexOf("}");

        if (start == -1 || end == -1 || end <= start) {
            throw new RuntimeException("JSON을 찾을 수 없습니다. 원본 응답: " + raw);
        }

        // 3) JSON 범위만 추출
        String json = raw.substring(start, end + 1).trim();

        // 4) 유효성 검사
        try {
            new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("추출한 JSON 파싱 실패: " + json, e);
        }

        return json;
    }


    /** escape */
    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
