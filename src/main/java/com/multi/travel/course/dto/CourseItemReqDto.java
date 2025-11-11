package com.multi.travel.course.dto;

import lombok.*;

/**
 * 코스에 들어갈 항목 오청 dto
 *
 * @author : seunga03
 * @filename : CourseItemReqDto
 * @since : 2025-11-08 토요일
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourseItemReqDto {
//    private Long courseId;
    private String categoryCode; // 기존의 placeType을 카테고리 코드로 변경
    private Long placeId; // 관광지/숙소의 ID
    private Integer orderNo; // 순서
    private Integer dayNo;  // 몇 일차에 속하는지
}
