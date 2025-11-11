package com.multi.travel.admin.controller;

import com.multi.travel.common.ResponseDto;
import com.multi.travel.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Please explain the class!!!
 *
 * @author : chang
 * @filename : adminController
 * @since : 2025-11-10 월요일
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    //private final ReviewService reviewService;
    private final MemberService memberService;


//    @PreAuthorize("hasRole('ADMIN')")  //  관리자만 가능
//    @GetMapping("/reviews/{id}")
//    public ResponseEntity<ReviewDetailDto> getReviewById(@PathVariable Long id) {
//        ReviewDetailDto review = reviewService.getReviewById(id);
//        return ResponseEntity.ok(review);
//
//    }

//    @PreAuthorize("hasRole('ADMIN')")  //  관리자만 가능
//    @GetMapping("/reviews")
//    public ResponseEntity<List<ReviewDetailDto>> getAllReviews() {
//        List<ReviewDetailDto> reviews = reviewService.getAllReviews();
//        return ResponseEntity.ok(reviews);
//    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/members")
    public ResponseEntity<ResponseDto> getMembers() {
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK,"전체회원조회 성공",memberService.findAll())

        );
    }

    // 🔹 회원 삭제
    @PreAuthorize("hasRole('ADMIN')")  //  관리자만 가능
    @DeleteMapping("/members/{id}")
    public ResponseEntity<ResponseDto> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "회원 삭제 성공", null)
        );
    }

}

