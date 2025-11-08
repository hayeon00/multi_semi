package com.multi.travel.plan.controller;

import com.multi.travel.common.ResponseDto;
import com.multi.travel.plan.dto.PlanReqDto;
import com.multi.travel.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : PlanController
 * @since : 2025. 11. 8. 토요일
 */

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping("/plans")
    public ResponseEntity<ResponseDto> createPlan(@RequestBody PlanReqDto dto) {
        Long planId = planService.createTripPlan(dto);
        return ResponseEntity.ok(new ResponseDto(HttpStatus.CREATED, "여행계획이 생성되었습니다.", planId));

    }

}

