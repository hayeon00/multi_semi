package com.multi.travel.ai.dto;

import com.multi.travel.course.dto.CourseItemReqDto;
import lombok.*;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AICourseResDto
 * @since : 2025-11-10 월요일
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AICourseResDto {
    private Long planId;
    private List<DayCourseDto> days;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DayCourseDto {
        private Integer dayNo;
        private List<CourseItemReqDto> items;
    }
}
