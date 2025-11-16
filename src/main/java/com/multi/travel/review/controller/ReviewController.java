package com.multi.travel.review.controller;

import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.review.dto.*;
import com.multi.travel.review.service.ReviewService;
import jakarta.validation.Valid;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 등록 (단일 리뷰)
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

    // 복합 리뷰 등록
    @PostMapping("/complex")
    public ResponseEntity<String> createComplexReview(
            @RequestPart("dto") @Valid ComplexReviewReqDto dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomUser user) {

        reviewService.createComplexReview(dto, images, user.getUserId());
        return ResponseEntity.ok("복합 리뷰가 성공적으로 등록되었습니다.");
    }


    // Target 기준 리뷰 조회 (코스/관광지)
    @GetMapping("/target") // 경로를 /reviews/target 으로 수정
    public ResponseEntity<Page<ReviewDetailDto>> getReviewsByTarget(
            // @PathVariable 대신 @RequestParam 사용
            @RequestParam("type") String targetType,
            @RequestParam("id") Long targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        log.debug("🔑 Target 리뷰 조회 요청 type: {}, id: {}", targetType, targetId);

        String[] sortParams = sort.split(",");
        Sort sorting = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        Pageable pageable = PageRequest.of(page, size, sorting);

        // Service 메서드 호출은 동일
        Page<ReviewDetailDto> reviews = reviewService.getReviewsByTarget(targetType, targetId, pageable);
        return ResponseEntity.ok(reviews);
    }



    // 사용자 기준 리뷰 조회 (마이페이지)
    @GetMapping("/my")
    public ResponseEntity<Page<ReviewDetailDto>> getReviewsByUser(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort sorting = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<ReviewDetailDto> reviews = reviewService.getReviewsByUser(user.getUserId(), pageable);
        return ResponseEntity.ok(reviews);
    }




    @GetMapping("/course")
    public ResponseEntity<ReviewTargetDto> getCourseReviewTarget(@RequestParam("planId") Long planId) {
        ReviewTargetDto courseTarget = reviewService.getCourseReviewTarget(planId);
        return ResponseEntity.ok(courseTarget);
    }


    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId, @AuthenticationPrincipal CustomUser user) {
        reviewService.deleteReview(reviewId, user.getUserId());
        return ResponseEntity.ok("리뷰가 성공적으로 삭제되었습니다.");
    }


    // 복합 리뷰 수정 API
    @PutMapping("/{reviewId}")
    public ResponseEntity<String> updateComplexReview(
            @PathVariable Long reviewId,
            @RequestPart("dto") @Valid ComplexReviewReqDto dto,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
            @AuthenticationPrincipal CustomUser user) {

        log.debug("🔑 리뷰 수정 요청 reviewId: {}", reviewId);
        log.debug("👤 인증된 사용자 userId: {}", user.getUserId());

        reviewService.updateComplexReview(reviewId, dto, newImages, user.getUserId());
        return ResponseEntity.ok("리뷰가 성공적으로 수정되었습니다.");
    }


    // ==========================================================
    // ⭐ 복합 리뷰 수정 데이터 로딩 API (SpotReviewDto에 imageUrls 매핑 추가)
    // ==========================================================
    @GetMapping("/plan/{planId}/complex-edit")
    public ResponseEntity<Map<String, Object>> getComplexReviewForEdit(
            @PathVariable Long planId,
            @AuthenticationPrincipal CustomUser user) {

        log.debug("🔑 리뷰 수정 데이터 로딩 요청 planId: {}", planId);

        List<ReviewDetailDto> allReviews = reviewService.getAllReviewsByPlanForEdit(planId, user.getUserId());

        ReviewDetailDto mainReview = null;
        List<SpotReviewDto> spotReviews = new ArrayList<>();
        String courseTitle = "코스 리뷰";

        for (ReviewDetailDto review : allReviews) {
            if ("course".equalsIgnoreCase(review.getTargetType())) {
                mainReview = review;
                courseTitle = mainReview.getTitle();

            } else {
                // 나머지 타입(예: 'tsp', 'acc')은 스팟 리뷰로 변환
                SpotReviewDto spotDto = SpotReviewDto.builder()
                        .reviewId(review.getReviewId())
                        .placeTitle(review.getTitle()) // ReviewDetailDto의 title을 placeTitle로 사용
                        .targetType(review.getTargetType())
                        .targetId(review.getTargetId())
                        .rating(review.getRating())
                        .content(review.getContent())
                        .imageUrls(review.getImageUrls())
                        .build();
                spotReviews.add(spotDto);
            }
        }

        if (mainReview == null) {
            log.warn("🚨 Plan {}에 대한 메인 리뷰(course)를 찾을 수 없음", planId);
            // 메인 리뷰가 없는 경우 404를 반환
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("mainReview", mainReview);
        responseData.put("spotReviews", spotReviews);
        responseData.put("courseTitle", courseTitle);

        return ResponseEntity.ok(responseData);
    }
}