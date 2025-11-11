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
            // 1. 요청 본문 구성
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

            // 4. 응답 파싱
            String text = extractText(response.body());
            log.info("Gemini 응답 수신 완료");
            return cleanResponse(text);

        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생", e);
            throw new RuntimeException("Gemini API 호출 실패", e);
        }
    }

    /** 응답 본문에서 text 필드 추출 */
    private String extractText(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                return candidates.get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text")
                        .asText();
            }
            throw new RuntimeException("Gemini 응답에 text 필드가 없습니다: " + json);
        } catch (Exception e) {
            throw new RuntimeException("Gemini 응답 파싱 실패", e);
        }
    }

    /** ```json``` 또는 불필요한 개행 제거 */
    private String cleanResponse(String text) {
        if (text == null) return "";
        return text
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .replaceAll("\n", "")
                .trim();
    }

    /** 큰따옴표 등 escape 처리 */
    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
