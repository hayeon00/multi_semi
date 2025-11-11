package com.multi.travel.review.controller;

import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.review.dto.ReviewDetailDto;
import com.multi.travel.review.dto.ReviewReqDto;
import com.multi.travel.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping
    public ResponseEntity<ReviewDetailDto> createReview(
            @ModelAttribute ReviewReqDto dto,
            @RequestParam(value = "images",required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomUser user
    ) {
        log.debug("🔐 인증된 사용자 userId: {}", user.getUserId());
        System.out.println("🔐 인증된 사용자 userId: " + user.getUserId());

        ReviewDetailDto result = reviewService.createReview(dto, images, user.getUserId());
        return ResponseEntity.ok(result);
    }

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

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUser user
    ) {
        reviewService.deleteReview(reviewId, user.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReviewDetailDto>> getMyReviews(@AuthenticationPrincipal CustomUser user) {
        List<ReviewDetailDto> myReviews = reviewService.getReviewsByUser(user.getUserId());
        return ResponseEntity.ok(myReviews);
    }

    @GetMapping("/target")
    public ResponseEntity<List<ReviewDetailDto>> getReviewsByTarget(
            @RequestParam("type") String targetType,
            @RequestParam("id") Long targetId
    ) {
        List<ReviewDetailDto> reviews = reviewService.getReviewsByTarget(targetType, targetId);
        return ResponseEntity.ok(reviews);
    }

}
