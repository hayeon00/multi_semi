package com.multi.travel.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ReviewResDto
 * @since : 2025. 11. 8. 토요일
 */
@Data
@Builder
@AllArgsConstructor
public class ReviewResDto {
    private Long id;
    private String title;
    private String content;
    private int rating;
    private String targetType;
    private Long targetId;
    private String writerName;
    private LocalDateTime createdAt;
    private List<String> imageUrls;


}