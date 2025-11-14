package com.multi.travel.review.dto;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : SpotReviewDto
 * @since : 2025. 11. 14. 금요일
 */
import lombok.Data;
@Data
public class SpotReviewDto {
    private String targetType; // "tsp", "acc" 등
    private Long targetId;
    private int rating;
    private String content; // 한 줄 평
}
