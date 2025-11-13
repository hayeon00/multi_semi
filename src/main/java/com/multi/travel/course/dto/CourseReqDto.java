package com.multi.travel.course.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 코스 요청 dto
 *
 * @author : seunga03
 * @filename : CoutseReqDto
 * @since : 2025-11-08 토요일
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourseReqDto {
    private Long planId;
    private String memberId;
    private List<CourseItemReqDto> items; // 초기 코스 아이템 리스트
}
