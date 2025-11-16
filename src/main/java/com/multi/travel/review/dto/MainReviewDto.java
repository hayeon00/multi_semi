package com.multi.travel.review.dto;

/**
 * 복합 리뷰 중 메인이 되는 코스 리뷰 정보(리뷰 업데이트 시 사용)
 *
 * @author : hayeon
 * @filename : MainReviewDto
 * @since : 2025. 11. 14. 금요일
 */

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class MainReviewDto {
    private Long reviewId;
    private Long planId;
    private String targetType; // "course"
    private Long targetId;

    @NotBlank(message = "코스 리뷰 제목은 필수 입력 항목입니다.")
    private String title;
    private String content;
    private int rating;
}