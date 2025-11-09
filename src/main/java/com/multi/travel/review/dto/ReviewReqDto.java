package com.multi.travel.review.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : ReviewReqDt
 * @since : 2025. 11. 8. 토요일
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewReqDto {
    private Long tripPlanId;
    private String title;
    private String content;
    private int rating;

    // 이미지 여러 장 업로드
    private List<MultipartFile> reviewImages;

    // 추후 연결할 코스 ID 목록 (선택)
    private List<Long> courseIds;
}