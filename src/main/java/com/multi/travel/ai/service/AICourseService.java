package com.multi.travel.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multi.travel.acc.dto.AccHasDistanceProjection;
import com.multi.travel.acc.repository.AccRepository;
import com.multi.travel.ai.client.GeminiClient;
import com.multi.travel.ai.dto.AICourseFeedbackReqDto;
import com.multi.travel.ai.dto.AICourseResDto;
import com.multi.travel.course.dto.CourseItemReqDto;
import com.multi.travel.course.dto.CourseReqDto;
import com.multi.travel.course.dto.CourseResDto;
import com.multi.travel.course.service.CourseService;
import com.multi.travel.plan.entity.TripPlan;
import com.multi.travel.plan.repository.TripPlanRepository;
import com.multi.travel.tourspot.dto.TspHasDistanceProjection;
import com.multi.travel.tourspot.repository.TspRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AICourseService
 * @since : 2025-11-10 월요일
 */
@Service
@RequiredArgsConstructor
public class AICourseService {

    private final TripPlanRepository tripPlanRepository;
    private final CourseService courseService;
    private final GeminiClient geminiClient;
    private final TspRepository tspRepository;
    private final AccRepository accRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * AI 추천 코스 최초 생성
     */
    public AICourseResDto generateCourse(Long planId) {
        TripPlan plan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("TripPlan을 찾을 수 없습니다."));

        // 좌표 null 체크
        if (plan.getStartMapX() == null || plan.getStartMapY() == null) {
            throw new IllegalStateException("출발지 좌표가 없습니다. 여행 계획에서 지도 좌표를 설정해주세요.");
        }

        long days = ChronoUnit.DAYS.between(plan.getStartDate(), plan.getEndDate()) + 1;
        BigDecimal mapx = plan.getStartMapX();
        BigDecimal mapy = plan.getStartMapY();
        String startLocation = Optional.ofNullable(plan.getStartLocation()).orElse("출발지 정보 없음");

        Pageable pageable = PageRequest.of(0, 30);

        // 주변 관광지 조회
        List<TspHasDistanceProjection> nearestSpots =
                tspRepository.findNearestWithDistanceRefactor(mapx, mapy, 0L, pageable);

        // 주변 숙소 조회
        List<AccHasDistanceProjection> nearestAccs =
                accRepository.findNearestWithDistanceAndStatus(mapx, mapy, pageable).getContent();

        // Null-safe 정보 보정
        String spotInfo = nearestSpots.stream()
                .limit(20)
                .map(s -> String.format(
                        "- 관광지ID: %d, 이름: %s, 주소: %s, 추천수: %d, 거리: %.2f km",
                        s.getId(),
                        Optional.ofNullable(s.getTitle()).orElse("이름없음"),
                        Optional.ofNullable(s.getAddress()).orElse("주소없음"),
                        s.getRecCount(),
                        s.getDistanceKm()
                ))
                .collect(Collectors.joining("\n"));

        String accInfo = nearestAccs.stream()
                .limit(10)
                .map(a -> String.format(
                        "- 숙소ID: %d, 이름: %s, 주소: %s, 추천수: %d, 거리: %.2f km",
                        a.getId(),
                        Optional.ofNullable(a.getTitle()).orElse("이름없음"),
                        Optional.ofNullable(a.getAddress()).orElse("주소없음"),
                        a.getRecCount(),
                        a.getDistanceKm()
                ))
                .collect(Collectors.joining("\n"));

        // 프롬프트 구성
        String prompt = """
                 
                 
                 
                당신은 여행 코스를 생성하는 시스템입니다.
                       
                반드시 아래 JSON 스키마로만 응답해야 합니다.
                JSON 바깥의 설명, 텍스트, 해설, 불릿, 마크다운, 코드블록(```)은 절대 포함하지 마세요.
                응답은 '{' 로 시작해서 '}' 로 끝나야 합니다.
                       
                ---------------------------------------
                [반드시 따라야 하는 출력 JSON 구조]
                       
                {
                  "planId": 123,
                  "days": [
                    {
                      "dayNo": 1,
                      "items": [
                        {
                           "categoryCode": "tsp",
                           "placeId": 0,
                           "placeName": "예시",
                           "orderNo": 1
                        } 
                      ]
                    }
                  ]
                }
                       
                ---------------------------------------
                규칙:
                - planId: 입력값 그대로 사용
                - days: 여행 일수만큼 생성
                - dayNo: 1부터 시작하여 하루씩 증가
                - orderNo: 각 day의 items 배열에서 1부터 시작하여 순서대로 증가하는 정수
                - 반드시 orderNo 필드를 포함해야 함
                - items: CourseItemReqDto 구조를 따름
                    - categoryCode: 관광지는 "tsp", 숙소는 "acc"
                    - placeId: 제공된 관광지ID 또는 숙소ID를 사용
                    - placeName: 제공된 이름(title)을 그대로 사용
                - 하루 최대 관광지 3개 + 숙소 1개
                - 출력 JSON 구조, 필드명 절대 변경 금지
                - JSON only. 설명/문장/부가텍스트 금지
                - 코드 블록 금지
                - JSON 외 텍스트 포함 시 잘못된 응답으로 처리됨
                       
                여행 정보:
                - planId: %d
                - 일정: %d일 (%s ~ %s)
                - 출발지: %s
                - 출발 좌표: (%.5f, %.5f)
                       
                [근처 관광지 목록]
                %s
                       
                [근처 숙소 목록]
                %s
                       
                위 규칙을 엄격히 지켜 AICourseResDto 구조 형태의 JSON만 출력하세요.
                               
                               

                 """.formatted(
                planId,                     // 1
                days,                       // 2
                plan.getStartDate(),        // 3
                plan.getEndDate(),          // 4
                startLocation,              // 5
                mapx,                       // 6
                mapy,                       // 7
                spotInfo,                   // 8
                accInfo                     // 9
        );

        try {
            String aiJson = geminiClient.generate(prompt);

            AICourseResDto dto = mapper.readValue(aiJson, AICourseResDto.class);

            // dayNo 보정
            if (dto.getDays() != null) {
                dto.getDays().forEach(day -> {
                    if (day.getItems() != null) {
                        day.getItems().forEach(item -> item.setDayNo(day.getDayNo()));
                    }
                });
            }

            return dto;

        } catch (Exception e) {
            throw new RuntimeException("AI 응답 파싱 실패", e);
        }
    }

    /**
     * 피드백 기반 재생성
     */
    public AICourseResDto regenerateCourseWithFeedback(AICourseFeedbackReqDto req) {
        try {
            String baseCourseJson = mapper.writeValueAsString(req.getBaseCourse());

            String prompt = """
                        당신은 여행 코스를 수정하는 시스템입니다.
                            
                        반드시 아래 JSON 스키마로만 응답해야 합니다.
                        JSON 바깥의 설명, 텍스트, 해설, 불릿, 마크다운, 코드블록(```)은 절대 포함하지 마세요.
                        응답은 '{' 로 시작해서 '}' 로 끝나야 합니다.
                            
                        ---------------------------------------
                        [반드시 따라야 하는 출력 JSON 구조]
                            
                        {
                          "planId": 123,
                          "days": [
                            {
                              "dayNo": 1,
                              "items": [
                                {
                                  "categoryCode": "tsp",
                                  "placeId": 0,
                                  "placeName": "예시",
                                  "orderNo": 1
                                }
                              ]
                            }
                          ]
                        }
                        ---------------------------------------
                            
                        규칙:
                        - 입력으로 제공된 기존 코스를 기반으로 수정합니다.
                        - planId는 반드시 기존 값과 동일해야 합니다.
                        - days 배열은 기존 days 개수를 유지해야 합니다.
                        - dayNo 값은 기존과 동일한 번호를 유지해야 합니다.
                        - orderNo는 반드시 포함하며, items 배열에서 1부터 증가하는 정수
                        - items의 categoryCode, placeId, placeName만 사용하여 코스를 조정합니다.
                        - JSON 구조 변경 금지 (필드명, 계층 구조 모두 변경 불가)
                        - JSON only. 설명/문장/부가 텍스트 금지.
                        - JSON 외 텍스트 포함 시 잘못된 응답으로 처리됩니다.
                            
                        [기존 JSON 코스]
                        %s
                            
                        [사용자 피드백]
                        %s
                            
                        위의 기존 코스를 기반으로 피드백을 반영하여
                        수정된 AICourseResDto 구조의 JSON만 출력하세요.
                    """.formatted(
                    baseCourseJson,
                    req.getFeedback()
            );


            String aiJson = geminiClient.generate(prompt);

            AICourseResDto dto = mapper.readValue(aiJson, AICourseResDto.class);

            if (dto.getDays() != null) {
                dto.getDays().forEach(day ->
                        day.getItems().forEach(i -> i.setDayNo(day.getDayNo()))
                );
            }

            return dto;

        } catch (Exception e) {
            throw new RuntimeException("AI 피드백 반영 실패", e);
        }
    }

    /**
     * AI 코스 확정
     */
    @Transactional
    public CourseResDto confirmCourse(AICourseResDto dto) {
        CourseReqDto req = new CourseReqDto();
        req.setPlanId(dto.getPlanId());

        List<CourseItemReqDto> items = dto.getDays().stream()
                .flatMap(day -> {
                    AtomicInteger orderCounter = new AtomicInteger(1);  // 일차별 순번 초기화

                    return day.getItems().stream().peek(item -> {
                        // 🟡 dayNo 보정
                        item.setDayNo(day.getDayNo());

                        // 🟡 categoryCode 보정
                        String code = item.getCategoryCode();
                        if (code == null || code.isBlank()) {
                            try {
                                var field = item.getClass().getDeclaredField("type");
                                field.setAccessible(true);
                                Object typeVal = field.get(item);
                                if (typeVal != null) {
                                    String t = typeVal.toString().toLowerCase();
                                    code = t.contains("acc") ? "acc" : "tsp";
                                }
                            } catch (Exception ignored) {}
                        }
                        if (code == null) code = "tsp";
                        item.setCategoryCode(code);

                        // orderNo 자동 보정 (가장 중요)
                        if (item.getOrderNo() == null) {
                            item.setOrderNo(orderCounter.getAndIncrement());
                        }

                    });
                })
                .collect(Collectors.toList());

        req.setItems(items);
        return courseService.createCourse(req);
    }
}
