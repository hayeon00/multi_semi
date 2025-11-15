package com.multi.travel.review.dto;

import com.multi.travel.review.entity.Review;
import com.multi.travel.review.entity.ReviewImage;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private String targetType;
    private Long targetId;
    private boolean isOwner;

    private ReviewDetailDto toDto(Review review) {
        return ReviewDetailDto.builder()
                .reviewId(review.getId())
                .title(review.getTitle())
                .content(review.getContent())
                .rating(review.getRating())
                .writer(review.getMember().getUsername())
                .createdAt(review.getCreatedAt())
                .targetType(review.getTargetType())
                .targetId(review.getTargetId())
                .imageUrls(review.getImages().stream()
                        .map(ReviewImage::getImageUrl)
                        .collect(Collectors.toList()))
                .build();
    }

}
