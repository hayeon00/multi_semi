package com.multi.travel.review.controller;

import com.multi.travel.review.dto.ReviewDetailDto;
import com.multi.travel.review.dto.ReviewReqDto;
import com.multi.travel.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : ReviewController
 * @since : 2025. 11. 8. 토요일
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    public ResponseEntity<Object> createReview(@ModelAttribute ReviewReqDto dto) {
        reviewService.createReview(dto);

        return ResponseEntity.ok().body("리뷰가 성공적으로 등록되었습니다.");
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewDetailDto>> getAllReviews() {
        List<ReviewDetailDto> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }



    @GetMapping("/reviews/{id}")
    public ResponseEntity<ReviewDetailDto> getReviewById(@PathVariable Long id) {
        ReviewDetailDto review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }


    @PutMapping("/reviews/{id}")
    public ResponseEntity<String> updateReview(@PathVariable Long id, @ModelAttribute ReviewReqDto dto) {
        reviewService.updateReview(id, dto);
        return ResponseEntity.ok("리뷰가 수정되었습니다.");
    }


    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok("리뷰가 삭제되었습니다.");
    }









}
