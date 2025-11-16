package com.multi.travel.review.controller;

import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.course.service.CourseService;
import com.multi.travel.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews/view")
public class ReviewFrontController {

    private final ReviewService reviewService;
    private final CourseService courseService;

    //내가 쓴 리뷰 페이지 조회
    @GetMapping("/search/my")
    public String showMyReviewPage(Model model, @AuthenticationPrincipal CustomUser user) {
        return "review/my-course-review-list";
    }

    //코스 리뷰 등록 페이지 출력
    @GetMapping("/write")
    public String showReviewWritePage(@RequestParam("planId") Long planId, Model model) {
        model.addAttribute("planId", planId);
        return "review/course-review-regist";
    }

    //[수정 핵심] 마이페이지 클릭 시 진입점: 등록/수정 분기 처리
    @GetMapping("/detail")
    public String determineReviewAction(@RequestParam("planId") Long planId,
                                        @AuthenticationPrincipal CustomUser user,
                                        Model model) {

        // 1. 해당 PlanId로 이미 작성된 리뷰(코스 리뷰 및 스팟 리뷰)가 있는지 확인합니다.
        // ReviewService.java에 이 메서드가 구현되어 있어야 합니다.
        boolean exists = reviewService.hasReviewForPlan(planId, user.getUserId());

        // 2. 리뷰 존재 여부에 따라 리디렉션 경로를 결정합니다.
        if (exists) {
            // ⭐ 리뷰가 있으면 -> 수정 페이지로 리디렉션
            return "redirect:/reviews/view/edit?planId=" + planId;
        } else {
            // ⭐ 리뷰가 없으면 -> 등록 페이지로 리디렉션
            return "redirect:/reviews/view/write?planId=" + planId;
        }
    }

    //[수정] 리뷰 수정 페이지 출력: planId를 사용하도록 변경
    @GetMapping("/edit")
    public String showEditPage(@RequestParam("planId") Long planId,
                               @AuthenticationPrincipal CustomUser user,
                               Model model) {

        model.addAttribute("planId", planId);

        return "review/course-review-edit";
    }

}