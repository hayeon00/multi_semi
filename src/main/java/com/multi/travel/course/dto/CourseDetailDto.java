package com.multi.travel.course.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : CourseDetailDto
 * @since : 2025. 11. 14. 금요일
 */

@Data
@Builder
public class CourseDetailDto {
    private Long courseId;          // 코스 ID
    private String title;           // 코스 제목
    private List<CourseItemDto> items; // 방문한 관광지 리스트
}
