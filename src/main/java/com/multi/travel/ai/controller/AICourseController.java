package com.multi.travel.ai.controller;

import com.multi.travel.ai.dto.AICourseFeedbackReqDto;
import com.multi.travel.ai.dto.AICourseResDto;
import com.multi.travel.ai.service.AICourseService;
import com.multi.travel.common.ResponseDto;
import com.multi.travel.course.dto.CourseResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AICourseController
 * @since : 2025-11-10 월요일
 */
@RestController
@RequestMapping("/ai/courses")
@RequiredArgsConstructor
public class AICourseController {

    private final AICourseService aiCourseService;

    /** 계획 기반 AI 코스 생성 */
    @PostMapping("/plan/{planId}")
    public ResponseEntity<ResponseDto> generateCourse(@PathVariable Long planId) {
        AICourseResDto result = aiCourseService.generateCourse(planId);
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "AI 추천 코스 생성 성공", result));
    }

    /** 피드백 기반 재추천 */
    @PostMapping("/feedback")
    public ResponseEntity<ResponseDto> regenerateCourse(@RequestBody AICourseFeedbackReqDto req) {
        AICourseResDto result = aiCourseService.regenerateCourseWithFeedback(req);
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "AI 피드백 기반 코스 재생성 성공", result));
    }

    /** AI 생성 코스 확정 (DB 저장) */
    @PostMapping("/confirm")
    public ResponseEntity<ResponseDto> confirmCourse(@RequestBody AICourseResDto dto) {
        CourseResDto saved = aiCourseService.confirmCourse(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto(HttpStatus.CREATED, "AI 코스 확정 완료", saved));
    }
}
