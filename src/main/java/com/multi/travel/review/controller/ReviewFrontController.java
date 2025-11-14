package com.multi.travel.review.controller;

import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.course.service.CourseService;
import com.multi.travel.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews/view")
public class ReviewFrontController {

    private final ReviewService reviewService;
    private final CourseService courseService;

    /** 내가 쓴 리뷰 페이지 출력 */
    @GetMapping("/search/my")
    public String showMyReviewPage(Model model, @AuthenticationPrincipal CustomUser user) {
        // model.addAttribute("userId", user.getUsername()); // 필요시
        return "review/my-course-review-list";
    }

    /** 코스 리뷰 등록 페이지 출력 */
    @GetMapping("/write")
    public String showReviewWritePage(@RequestParam("planId") Long planId, Model model) {
        model.addAttribute("planId", planId);
        return "review/course-review-regist";
    }

    /** 코스 리뷰 상세 페이지 출력 */
    @GetMapping("/detail")
    public String showCourseReviewDetailPage(@RequestParam("reviewId") Long reviewId, Model model) {
        model.addAttribute("reviewId", reviewId);
        return "course-review-detail";
    }












    /** 코스 리뷰 조회 페이지 출력 */
    @GetMapping("/search/{courseId}")
    public String showCourseReviewPage(@PathVariable Long courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "review/course-review-list";
    }












}
