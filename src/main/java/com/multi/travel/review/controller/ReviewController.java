package com.multi.travel.review.controller;

import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.review.dto.ReviewDetailDto;
import com.multi.travel.review.dto.ReviewReqDto;
import com.multi.travel.review.dto.ReviewTargetDto;
import com.multi.travel.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 등록
    @PostMapping
    public ResponseEntity<ReviewDetailDto> createReview(
            @ModelAttribute ReviewReqDto dto,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomUser user
    ) {
        log.debug("🔐 인증된 사용자 userId: {}", user.getUserId());
        ReviewDetailDto result = reviewService.createReview(dto, images, user.getUserId());
        return ResponseEntity.ok(result);
    }

    // 리뷰 수정
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDetailDto> updateReview(
            @PathVariable Long reviewId,
            @ModelAttribute ReviewReqDto dto,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomUser user
    ) {
        ReviewDetailDto updated = reviewService.updateReview(reviewId, dto, images, user.getUserId());
        return ResponseEntity.ok(updated);
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUser user
    ) {
        reviewService.deleteReview(reviewId, user.getUserId());
        return ResponseEntity.ok("리뷰가 성공적으로 삭제되었습니다.");
    }

    //리뷰 상세조회
//    @GetMapping("/detail/{reviewId}")
//    public ResponseEntity<ReviewDetailResponseDto> getReviewDetail(
//            @PathVariable Long reviewId,
//            @AuthenticationPrincipal CustomUser user
//    ) {
//        ReviewDetailResponseDto dto = reviewService.getReviewDetail(reviewId, user.getUserId());
//        return ResponseEntity.ok(dto);
//    }


    // 내가 쓴 리뷰 조회 (페이징)
    @GetMapping("/my")
    public ResponseEntity<Page<ReviewDetailDto>> getMyReviews(
            @AuthenticationPrincipal CustomUser user,
            Pageable pageable
    ) {
        Pageable fixedPageable = PageRequest.of(pageable.getPageNumber(), 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewDetailDto> myReviews = reviewService.getReviewsByUser(user.getUserId(), fixedPageable);
        return ResponseEntity.ok(myReviews);
    }

    // 타겟별 리뷰 조회 (페이징)
    @GetMapping("/targets")
    public ResponseEntity<List<ReviewTargetDto>> getReviewTargetsByTripPlan(
            @RequestParam("planId") Long planId,
            @AuthenticationPrincipal CustomUser user
    ) {
        List<ReviewTargetDto> targets = reviewService.getReviewTargetsByPlan(planId, user.getUserId());
        return ResponseEntity.ok(targets);
    }

    // ReviewController.java
    @GetMapping("/course")
    public ResponseEntity<ReviewTargetDto> getCourseReviewTarget(@RequestParam("planId") Long planId) {
        ReviewTargetDto courseTarget = reviewService.getCourseReviewTarget(planId);
        return ResponseEntity.ok(courseTarget);
    }





}
