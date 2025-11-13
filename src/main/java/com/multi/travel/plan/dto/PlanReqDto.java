package com.multi.travel.plan.dto;

import com.multi.travel.course.dto.CourseReqDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : PlanRequestDto
 * @since : 2025. 11. 8. 토요일
 */

@Getter
@Setter
@Data
public class PlanReqDto {
    private String memberId;      // 사용자 ID
    private Long tourSpotId;    // 출발지 관광지 ID
    private String title;         // 여행 제목
    private int numberOfPeople;   // 인원 수
    private LocalDate startDate;  // 여행 시작일
    private LocalDate endDate; // 여행 종료일
    private CourseReqDto course;

}

