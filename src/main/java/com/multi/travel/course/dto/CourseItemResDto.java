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
    private String placeType;
    private Long placeId;
    private Integer orderNo;
    private Integer dayNo;
}