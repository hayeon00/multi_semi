package com.multi.travel.review.dto;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : ReviewTargetDto
 * @since : 2025. 11. 14. 금요일
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 리뷰를 작성할 수 있는 대상 정보 DTO
 * (ex. 코스 / 관광지 / 숙소 등)
 */

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewTargetDto {

    private String type;     // 예: course, tsp
    private Long targetId;   // 코스 ID 또는 장소 ID
    private String title;    // 사용자에게 보여줄 제목 (장소명 또는 "코스")

    public static ReviewTargetDto of(String type, Long targetId, String title) {
        return ReviewTargetDto.builder()
                .type(type)
                .targetId(targetId)
                .title(title)
                .build();
    }
}