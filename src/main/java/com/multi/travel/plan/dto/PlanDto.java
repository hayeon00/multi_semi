package com.multi.travel.plan.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : PlanDto
 * @since : 2025. 11. 14. 금요일
 */

@Builder
@Data
public class PlanDto {
    private Long planId;        // 여행 계획 ID
    private String title;       // 여행 제목
    private String startDate;   // 여행 시작일 (yyyy-MM-dd)
    private String endDate;     // 여행 종료일 (yyyy-MM-dd)
    private int days;           // 총 여행일 수
}
