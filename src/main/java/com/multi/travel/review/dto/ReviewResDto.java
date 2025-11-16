package com.multi.travel.review.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 리뷰 조회 결과를 클라이언트에 전달할 때 사용되는 응답 DTO
 * 마이페이지에서 작성한 리뷰 목록 조회 시 응답 형태
 * @author : hayeon
 * @filename : ReviewResDto
 * @since : 2025. 11. 8. 토요일
 */
@Data
@Builder
@AllArgsConstructor
@Getter
@Setter
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