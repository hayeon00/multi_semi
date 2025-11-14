package com.multi.travel.plan.controller;

import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.common.ResponseDto;
import com.multi.travel.plan.dto.PlanDetailResDto;
import com.multi.travel.plan.dto.PlanReqDto;
import com.multi.travel.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<ResponseDto> getPlans(@AuthenticationPrincipal CustomUser user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDto(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.", null));
        }

        try {
            // 사용자 ID로 계획 목록 조회
            List<PlanDetailResDto> plans = planService.getPlansByUser(user.getUserId());

            // 계획이 없는 경우 클라이언트에게 안내 가능 (선택)
            if (plans.isEmpty()) {
                return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "여행 계획이 존재하지 않습니다.", plans));
            }

            return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "여행 계획 목록 조회 성공", plans));
        } catch (Exception e) {
            // 예외 로깅 추가
            e.printStackTrace(); // 실제로는 log.error() 권장
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.", null));
        }
    }


    @PostMapping
    public ResponseEntity<ResponseDto> createPlan(@RequestBody PlanReqDto dto,
                                                  @AuthenticationPrincipal CustomUser user) {

        // 로그인 정보에서 email(=loginId)을 가져와 memberId에 세팅
        dto.setMemberId(user.getUserId());

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

    @PutMapping("/{planId}")
    public ResponseEntity<ResponseDto> updatePlan(@PathVariable Long planId,
                                                  @RequestBody PlanReqDto dto,
                                                  @AuthenticationPrincipal CustomUser user) {

        planService.updateTripPlan(planId, dto, user.getUserId());
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "여행 계획이 수정되었습니다.", null));
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<ResponseDto> deletePlan(@PathVariable Long planId,
                                                  @AuthenticationPrincipal CustomUser user) {

        planService.deleteTripPlan(planId, user.getUserId());
        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "여행 계획이 삭제되었습니다.", null));
    }


    @PostMapping("/{planId}/course")
    public ResponseEntity<ResponseDto> attachCourseToPlan(
            @PathVariable Long planId,
            @RequestParam Long courseId
    ) {
        planService.attachCourse(planId, courseId);

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "코스가 계획에 성공적으로 추가되었습니다.", null)
        );
    }


}

