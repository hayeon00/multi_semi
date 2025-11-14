package com.multi.travel.review.dto;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : MainReviewDto
 * @since : 2025. 11. 14. 금요일
 */
import lombok.Data;
@Data
public class MainReviewDto {
    private String targetType; // "course"
    private Long targetId;
    private String title;
    private String content;
    private int rating;
}