package com.multi.travel.plan.controller;

import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.plan.dto.PlanDetailResDto;
import com.multi.travel.common.ResponseDto;
import com.multi.travel.plan.dto.PlanReqDto;
import com.multi.travel.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<ResponseDto> createPlan(@RequestBody PlanReqDto dto,
                                                  @AuthenticationPrincipal CustomUser user) {

        // 로그인 정보에서 email(=loginId)을 가져와 memberId에 세팅
        dto.setMemberId(user.getEmail());

        // 임시 관광지 ID 강제 설정 (아직 상세페이지 미구현이므로)
        if (dto.getTourSpotId() == null) {
            dto.setTourSpotId(11L); // 임시 기본 관광지 ID --> 나중에는 삭제하고 관광지 상세 페이지에서 값 받아와야 함
        }

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

