package com.multi.travel.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    /** AI 추천 코스 생성 */
    public AICourseResDto generateCourse(Long planId) {
        TripPlan plan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("TripPlan을 찾을 수 없습니다."));

        long days = ChronoUnit.DAYS.between(plan.getStartDate(), plan.getEndDate()) + 1;

        // 출발 좌표 기준
        BigDecimal mapx = plan.getStartMapX();
        BigDecimal mapy = plan.getStartMapY();
        String startLocation = plan.getStartLocation();

        // 이미 정의된 쿼리 재활용
        Pageable pageable = PageRequest.of(0, 30); // 가까운 30개만
        List<Object[]> nearestSpotsRaw = tspRepository.findNearestWithDistance(mapx, mapy, 0L, pageable);
        List<Object[]> nearestAccsRaw = accRepository.findNearestWithDistance(mapx, mapy, 0L, pageable);

        // 결과 요약
        String spotInfo = nearestSpotsRaw.stream()
                .limit(20)
                .map(obj -> String.format("- 관광지ID: %s (거리: %.2f km)", obj[0], obj[1]))
                .collect(Collectors.joining("\n"));

        String accInfo = nearestAccsRaw.stream()
                .limit(10)
                .map(obj -> String.format("- 숙소ID: %s (거리: %.2f km)", obj[0], obj[1]))
                .collect(Collectors.joining("\n"));

        // 프롬프트 구성
        String prompt = """
        당신은 여행 동선을 최적화하는 여행 플래너입니다.
        여행 일정: %d일 (%s ~ %s)
        출발지: %s
        출발 좌표: (%.5f, %.5f)
        
        아래는 출발지 기준 가까운 관광지 및 숙소입니다.
        이동 동선이 자연스럽도록 계획을 세워주세요.

        [근처 관광지 목록]
        %s

        [근처 숙소 목록]
        %s

        조건:
        - 하루 최대 관광지 3곳, 숙소 1곳 포함
        - 이동 동선이 짧을수록 우선 배치
        - JSON 형식으로만 응답하세요.

        예시 형식:
        {
          "planId": %d,
          "days": [
            {
              "dayNo": 1,
              "items": [
                {"categoryCode": "tsp", "placeId": 1, "orderNo": 1},
                {"categoryCode": "tsp", "placeId": 2, "orderNo": 2},
                {"categoryCode": "acc", "placeId": 3, "orderNo": 3}
              ]
            }
          ]
        }
    """.formatted(
                days,
                plan.getStartDate(), plan.getEndDate(),
                startLocation,
                mapx, mapy,
                spotInfo,
                accInfo,
                plan.getId()
        );

        // Gemini 호출
        String response = geminiClient.generate(prompt);

        try {
            ObjectMapper mapper = new ObjectMapper();
            AICourseResDto dto = mapper.readValue(response, AICourseResDto.class);

            // dayNo 누락 보정 로직 추가
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


    /** 피드백 반영 재생성 */
    public AICourseResDto regenerateCourseWithFeedback(AICourseFeedbackReqDto req) {
        String prompt = """
        다음 피드백을 반영해 기존 여행 코스를 수정해주세요.
        
        계획 ID: %d
        피드백: %s
        
        기존 여행 계획의 일정(시작일~종료일)과 출발지는 유지하되,
        피드백을 반영해 수정된 여행 코스를 JSON 형식으로 반환해주세요.

        예시 형식:
        {
          "planId": %d,
          "days": [
            {
              "dayNo": 1,
              "items": [
                {"categoryCode": "tsp", "placeId": 1, "orderNo": 1},
                {"categoryCode": "acc", "placeId": 3, "orderNo": 2}
              ]
            }
          ]
        }
    """.formatted(req.getPlanId(), req.getFeedback(), req.getPlanId());

        String response = geminiClient.generate(prompt);

        try {
            ObjectMapper mapper = new ObjectMapper();
            AICourseResDto dto = mapper.readValue(response, AICourseResDto.class);

            // dayNo 보정 (혹시 누락된 경우 대비)
            if (dto.getDays() != null) {
                dto.getDays().forEach(day -> {
                    if (day.getItems() != null) {
                        day.getItems().forEach(item -> item.setDayNo(day.getDayNo()));
                    }
                });
            }

            return dto;
        } catch (Exception e) {
            throw new RuntimeException("AI 피드백 반영 실패", e);
        }
    }


    /** 코스 확정 (DB 저장) */
    @Transactional
    public CourseResDto confirmCourse(AICourseResDto dto) {
        // AI 응답을 기존 CourseService.createCourse() 로 재활용
        CourseReqDto req = new CourseReqDto();
        req.setPlanId(dto.getPlanId());

        List<CourseItemReqDto> items = dto.getDays().stream()
                .flatMap(day -> day.getItems().stream()
                        .map(item -> {
                            item.setDayNo(day.getDayNo());
                            return item;
                        }))
                .collect(Collectors.toList());

        req.setItems(items);
        return courseService.createCourse(req);
    }
}
