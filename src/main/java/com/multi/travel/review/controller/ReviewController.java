package com.multi.travel.review.controller;

import com.multi.travel.review.dto.ReviewReqDto;
import com.multi.travel.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
