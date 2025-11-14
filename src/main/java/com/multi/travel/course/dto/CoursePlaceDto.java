package com.multi.travel.course.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 코스 목록을 상세조회
 *
 * @author : hayeon
 * @filename : CoursePlaceDto
 * @since : 2025. 11. 10. 월요일
 */
@Builder
@Getter
public class CoursePlaceDto {
    private Long id;          // 장소 ID
    private String type;      // TOUR_SPOT or ACCOMMODATION
    private String title;
    private String address;
    private String mapx;
    private String mapy;
    private Integer orderNo;
    private Integer dayNo;

}
