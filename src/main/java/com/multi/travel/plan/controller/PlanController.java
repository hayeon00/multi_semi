package com.multi.travel.plan.controller;

import com.multi.travel.plan.dto.PlanDetailResDto;
import com.multi.travel.common.ResponseDto;
import com.multi.travel.plan.dto.PlanReqDto;
import com.multi.travel.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : PlanController
 * @since : 2025. 11. 8. 토요일
 */

@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping
    public ResponseEntity<ResponseDto> createPlan(@RequestBody PlanReqDto dto) {
        Long planId = planService.createTripPlan(dto);
        return ResponseEntity.ok(new ResponseDto(HttpStatus.CREATED, "여행계획이 생성되었습니다.", planId));

    }

    @GetMapping("/{planId}")
    public ResponseEntity<ResponseDto> getPlanDetail(@PathVariable Long planId) {
        PlanDetailResDto detail = planService.getTripPlanDetail(planId);
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "여행 계획 상세정보 조회 성공", detail)
        );
    }

}

