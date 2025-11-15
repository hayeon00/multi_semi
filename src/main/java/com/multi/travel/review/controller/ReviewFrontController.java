package com.multi.travel.review.controller;

import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.course.service.CourseService;
import com.multi.travel.review.dto.ReviewDetailDto;
import com.multi.travel.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String showReviewDetail(@RequestParam Long reviewId,
                                   @AuthenticationPrincipal CustomUser user,
                                   Model model) {

        ReviewDetailDto review = reviewService.getReviewDetail(reviewId, user.getUserId());
        model.addAttribute("review", review);
        return "review/course-review-detail";
    }


    @GetMapping("/edit")
    public String showEditPage(@RequestParam Long reviewId, @AuthenticationPrincipal CustomUser user, Model model) {
        ReviewDetailDto review = reviewService.getReviewDetail(reviewId, user.getUserId());
        model.addAttribute("review", review);
        return "review/course-review-edit";
    }


//    @PostMapping("/{reviewId}")
//    public String updateReview(@PathVariable Long reviewId,
//                               @AuthenticationPrincipal CustomUser user,
//                               @ModelAttribute ReviewUpdateDto dto) {
//        reviewService.updateReview(reviewId, dto, user.getUserId());
//        return "redirect:/reviews/view/detail?reviewId=" + reviewId;
//    }














    /** 코스 리뷰 조회 페이지 출력 */
    @GetMapping("/search/{courseId}")
    public String showCourseReviewPage(@PathVariable Long courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "review/course-review-list";
    }












}
