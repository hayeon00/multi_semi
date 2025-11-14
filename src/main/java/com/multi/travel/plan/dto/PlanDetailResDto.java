package com.multi.travel.plan.dto;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : PlanDetailResDto
 * @since : 2025. 11. 10. 월요일
 */


import com.multi.travel.course.dto.CoursePlaceDto;
import com.multi.travel.course.entity.Course;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class PlanDetailResDto {
    private Long id;
    private String title;
    private String startLocation;
    private BigDecimal startMapX;
    private BigDecimal startMapY;
    private boolean isAiPlan;
    private char status;
    private int numberOfPeople;
    private LocalDate startDate;
    private LocalDate endDate;
    private String memberName;
    private Course course;          // 사용자 코스 정보
    private List<CoursePlaceDto> coursePlaces;
    private Long tourSpotId;


}


