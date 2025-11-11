package com.multi.travel.admin.controller;

import com.multi.travel.admin.controller.dto.TourSpotReqDto;
import com.multi.travel.admin.service.AdminService;
import com.multi.travel.common.ResponseDto;
import com.multi.travel.member.service.MemberService;
import com.multi.travel.review.service.ReviewService;
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
    private final ReviewService reviewService;
    private final MemberService memberService;
    private final AdminService adminService;




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

    //관광지 추가
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/tourspot")
    public ResponseEntity<ResponseDto> insertTourspot(@RequestBody TourSpotReqDto  tourSpotReqDto) {
        adminService.insertTourSpot(tourSpotReqDto);

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK,"관광지 추가 성공",null)

        );
    }

    //관광지 삭제
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/tourspot/{id}")
    public ResponseEntity<ResponseDto> deleteTourspot(@PathVariable Long id) {
        adminService.deleteSpot(id);
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "관광지 삭제 성공", null)
        );

    }

}

