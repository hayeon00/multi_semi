package com.multi.travel.review.dto;

import com.multi.travel.course.dto.CourseDetailDto;
import com.multi.travel.plan.dto.PlanDto;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ReviewDetailResponseDto
 * @since : 2025. 11. 14. 금요일
 */
public class ReviewDetailResponseDto {

    private ReviewDetailDto review;             // 리뷰 정보
    private PlanDto tripPlan;               // 여행 계획 정보
    private CourseDetailDto course;             // 코스 + 방문지 목록
}
