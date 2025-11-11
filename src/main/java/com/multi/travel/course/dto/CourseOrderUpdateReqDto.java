package com.multi.travel.course.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : CourseOrderUpdateReqDto
 * @since : 2025-11-08 토요일
 */
@Getter
@Setter
public class CourseOrderUpdateReqDto {

    private List<OrderUpdateItem> items;

    @Getter
    @Setter
    public static class OrderUpdateItem {
        private Long itemId;
        private Integer orderNo;
        private Integer dayNo;
    }
}