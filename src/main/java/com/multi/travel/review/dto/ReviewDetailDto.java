package com.multi.travel.review.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : ReviewDetailDto
 * @since : 2025. 11. 9. 일요일
 */

@Data
@Builder
public class ReviewDetailDto {
    private Long reviewId;
    private String title;
    private String content;
    private int rating;
    private String writer; // 예: 회원 이름
    private LocalDateTime createdAt;
    private List<String> imageUrls;
}
