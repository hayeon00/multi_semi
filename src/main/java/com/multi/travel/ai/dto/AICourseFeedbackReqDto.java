package com.multi.travel.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AICourseFeedbackReqDto
 * @since : 2025-11-10 월요일
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AICourseFeedbackReqDto {
    private Long planId;               // 어떤 여행 계획에 대한 코스인지
    private String feedback;           // 사용자 피드백
    private AICourseResDto baseCourse; // 현재 화면에서 보고 있는 기존 AI 코스
}
