package com.multi.travel.review.dto;

/**
 * 관광지에 대한 개별 리뷰 정보를 담는 DTO
 * "코스 리뷰"와 함께 등록되는 관광지 리뷰들을 처리할 때 사용(업데이트 요청/응답 모두 사용)
 *
 * @author : hayeon
 * @filename : SpotReviewDto
 * @since : 2025. 11. 14. 금요일
 */

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SpotReviewDto {
    private Long reviewId;
    private String placeTitle;
    private String targetType; // "tsp", "acc" 등
    private Long targetId;
    private int rating;
    private String content; // 한 줄 평

    private List<String> imageUrls;
}
