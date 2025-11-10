package com.multi.travel.course.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 코스에 들어갈 항목 오청 dto
 *
 * @author : lsa03
 * @filename : CourseItemRequestDto
 * @since : 2025-11-08 토요일
 */

@Getter
@Setter
public class CourseItemReqDto {
//    private Long courseId;
    private String placeType; // TOUR_SPOT or ACCOMMODATION
    private Long placeId; // 관광지/숙소의 ID
    private Integer orderNo; // 순서
}
