package com.multi.travel.review.controller;

import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.review.dto.ReviewReqDto;
import com.multi.travel.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews/view")
public class ReviewFrontController {

    private final ReviewService reviewService;


    /** 코스 리뷰 등록 페이지 출력 */
    @GetMapping("/regist/course/{courseId}")
    public String registCourseReviewPage(@PathVariable Long courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "review/courseReviewForm";
    }

    /** 코스 리뷰 상세 페이지 출력 */
    @GetMapping("/detail/{courseId}")
    public String showCourseReviewDetailPage(@PathVariable Long courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "review/courseReviewDetail";
    }

    /** 코스 리뷰 조회 페이지 출력 */
    @GetMapping("/search/{courseId}")
    public String showCourseReviewPage(@PathVariable Long courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "review/courseReviewList";
    }

    /** 내가 쓴 리뷰 페이지 출력 */
    @GetMapping("/search/my")
    public String showMyReviewPage(Model model, @AuthenticationPrincipal CustomUser user) {
       // model.addAttribute("userId", user.getUsername()); // 필요시
        return "review/mycourseReviewList";
    }









    /** 비동기(Ajax) 리뷰 등록 요청 처리 → JSON 응답 반환 */
    @PostMapping("/regist/course")
    @ResponseBody
    public ResponseEntity<?> submitCourseReview(@ModelAttribute ReviewReqDto dto,
                                                @RequestParam(value = "reviewImages", required = false) List<MultipartFile> files,
                                                @AuthenticationPrincipal CustomUser user) {
        try {
            dto.setTargetType("COURSE");
            reviewService.createReview(dto, files, user.getUsername());
            return ResponseEntity.ok().body("리뷰 등록 성공");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("리뷰 등록 실패: " + e.getMessage());
        }
    }



}
