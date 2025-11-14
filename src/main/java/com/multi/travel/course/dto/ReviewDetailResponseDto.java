package com.multi.travel.course.dto;

import com.multi.travel.plan.entity.TripPlan;
import com.multi.travel.review.dto.ReviewDetailDto;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ReviewDetailResponseDto
 * @since : 2025. 11. 14. 금요일
 */
public class ReviewDetailResponseDto {
    private ReviewDetailDto review;      // 리뷰 자체 정보
    private TripPlan tripPlan;        // 여행 계획 정보
    private CourseDetailDto course;      // 코스 + 관광지 목록 정보
}
