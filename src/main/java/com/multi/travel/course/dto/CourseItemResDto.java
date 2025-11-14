package com.multi.travel.course.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : CourseItemResDto
 * @since : 2025-11-08 토요일
 */
@Getter
@Setter
@Builder
public class CourseItemResDto {
    private Long itemId;
    private String categoryCode; // 기존의 placeType을 카테고리 코드로 변경
    private String categoryName; // 조회 시 사용자에게 "관광지" / "숙소" 등으로 표현 가능
    private Long placeId;
    private String placeTitle; // 코스 title 생성을 위해 추가함
    private Integer orderNo;
    private Integer dayNo;
    private String placeImageUrl;
}