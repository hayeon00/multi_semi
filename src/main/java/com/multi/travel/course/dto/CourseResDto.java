package com.multi.travel.course.dto;

import com.multi.travel.course.entity.CourseItem;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 코스 응답 dto
 *
 * @author : lsa03
 * @filename : CourseResponseDto
 * @since : 2025-11-08 토요일
 */
public class CourseResDto {
    private Long courseId;
    private Long planId;
    private String status;
    private LocalDateTime createdAt;
    private List<CourseItem> items;
}
