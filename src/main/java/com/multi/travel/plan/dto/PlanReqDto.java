package com.multi.travel.plan.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : PlanRequestDto
 * @since : 2025. 11. 8. 토요일
 */

@Data
public class PlanReqDto {
    private String memberId;
    private Long attractionId; //관광지 id-> 출발위치로 사용
    private String title;
    private int numberOfPeople;
    //private String startLocation;
    private LocalDate startDate;
    private LocalDate endDate;

}

