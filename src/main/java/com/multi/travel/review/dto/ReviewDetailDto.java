package com.multi.travel.review.dto;

import com.multi.travel.review.entity.Review;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 단일 리뷰 조회, 복합 리뷰 편집 시 메인/스팟 리뷰의 상세 정보 전달
 *
 * @author : hayeon
 * @filename : ReviewDetailDto
 * @since : 2025. 11. 9. 일요일
 */

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDetailDto {

    private Long reviewId;
    private String title;
    private String content;
    private int rating;
    private String writer;
    private LocalDateTime createdAt;

    private String targetType;
    private Long targetId;
    private Long planId;

    // 🔥 이미지 URL 목록
    private List<String> imageUrls;

    public ReviewDetailDto(Review review) {
        this.reviewId = review.getId();
        this.title = review.getTitle();
        this.content = review.getContent();
        this.rating = review.getRating();
        this.writer = review.getMember().getLoginId();
        this.createdAt = review.getCreatedAt();
        this.targetType = review.getTargetType();
        this.targetId = review.getTargetId();
        this.planId = review.getTripPlan().getId();
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}


