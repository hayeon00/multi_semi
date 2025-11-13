package com.multi.travel.admin.controller;

import com.multi.travel.admin.controller.dto.TourSpotReqDto;
import com.multi.travel.admin.service.AdminService;
import com.multi.travel.common.ResponseDto;
import com.multi.travel.member.service.MemberService;
import com.multi.travel.review.service.ReviewService;
import com.multi.travel.tourspot.dto.TourSpotDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 페이지 통합 컨트롤러
 * - View 렌더링(Thymeleaf) + REST API 기능 통합
 * - HTML 페이지 반환과 JSON 데이터 반환을 모두 처리
 *
 * @author chang
 * @since 2025-11-12
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final ReviewService reviewService;
    private final MemberService memberService;
    private final AdminService adminService;

    // -----------------------------------------------------------------------
    // ✅ [1] 관리자 페이지 뷰 렌더링
    // -----------------------------------------------------------------------

    /** 관리자 회원 목록 페이지 */
    @GetMapping("/view/members")
    public String memberListPage() {
        return "admin/member-list"; // → templates/admin/member-list.html
    }

    /** 관리자 관광지 목록 페이지 */
    @GetMapping("/view/tourspot")
    @PreAuthorize("hasRole('ADMIN')")
    public String tourspotListPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            Model model
    ) {
        // ✅ 관광지 목록 조회 (페이징 포함)
        if (page < 0) page = 0;

        Page<TourSpotDTO> tourPage = adminService.getAllTourSpotList(page, size, sort);
        int totalPages = tourPage.getTotalPages();

        // ✅ 블록 단위 페이지 계산 (한 번에 10개씩)
        int blockSize = 10;
        int currentBlock = page / blockSize;
        int startPage = currentBlock * blockSize;
        int endPage = Math.min(startPage + blockSize, totalPages);

        // ✅ View로 전달할 데이터 설정
        model.addAttribute("tourspotList", tourPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasNext", tourPage.hasNext());
        model.addAttribute("hasPrevious", tourPage.hasPrevious());
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "admin/tourspot_list"; // → templates/admin/tourspot_list.html
    }

    /** 관리자 관광지 추가 페이지 */
    @GetMapping("/view/tourspot/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String tourSpotAddPage() {
        return "admin/tourspot-add";
    }

    // -----------------------------------------------------------------------
    // ✅ [2] REST API (데이터 처리)
    // -----------------------------------------------------------------------

    /** 전체 회원 조회 */
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/members")
    public ResponseEntity<ResponseDto> getMembers() {
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "전체 회원 조회 성공", memberService.findAll())
        );
    }

    /** 회원 삭제 */
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/members/{id}")
    public ResponseEntity<ResponseDto> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "회원 삭제 성공", null)
        );
    }

    /** 관광지 추가 */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/tourspot")
    public ResponseEntity<ResponseDto> insertTourspot(@ModelAttribute TourSpotReqDto dto) {
        adminService.insertTourSpot(dto);
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "관광지 추가 성공", null)
        );
    }

    /** 관광지 삭제 */
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/tourspot/{id}")
    public ResponseEntity<ResponseDto> deleteTourspot(@PathVariable Long id) {
        adminService.deleteSpot(id);
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "관광지 삭제 성공", null)
        );
    }

    /** 관광지 수정 */
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/tourspot/{id}")
    public ResponseEntity<ResponseDto> updateTourSpot(
            @PathVariable Long id,
            @RequestBody TourSpotReqDto dto
    ) {
        adminService.updateSpot(id, dto);
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "관광지 수정 완료", null)
        );
    }

    /** 관광지 전체 조회 (JSON API) */
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/tourspot/list")
    public ResponseEntity<ResponseDto> getAllTourSpots(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                new ResponseDto(
                        HttpStatus.OK,
                        "전체 관광지 조회 성공",
                        adminService.getAllTourSpotList(page, size, sort)
                )
        );
    }

    /** 관광지 상세 조회 */
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/tourspot/{id}")
    public ResponseEntity<ResponseDto> getTourSpotDetail(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ResponseDto(
                        HttpStatus.OK,
                        "관광지 상세 조회 성공",
                        adminService.getTourSpotDetail(id)
                )
        );
    }
}
