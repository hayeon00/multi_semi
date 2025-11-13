package com.multi.travel.course.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 코스 응답 dto
 *
 * @author : seunga03
 * @filename : CourseResponseDto
 * @since : 2025-11-08 토요일
 */

@Getter
@Setter
@Builder
public class CourseResDto {
    private Long courseId;
//    private Long planId; --> 코스(1):플랜(N) 관계이기 때문에 planId를 둘 필요가 없음
    private String status;
    private LocalDateTime createdAt;
    private List<CourseItemResDto> items;
    private Integer recCount;
    private String creatorUserId;
}
