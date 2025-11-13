//package com.multi.travel.admin.controller;
//
//import com.multi.travel.admin.service.AdminService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//
///**
// * 관리자 페이지 View Controller
// * - HTML(Thymeleaf) 페이지 렌더링 전용
// * - 실제 데이터는 AdminController (REST API)가 담당
// *
// * @author chang
// * @since 2025-11-09
// */
//@Controller
//@RequestMapping("/admin/view")
//@RequiredArgsConstructor
//public class AdminViewController {
//
//    private final AdminService adminService;
//
//    /** ✅ 관리자 회원 목록 페이지 */
//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/members")
//    public String memberListPage() {
//        return "admin/member-list";  // → templates/admin/member-courseReviewList.html
//    }
//
//    /** ✅ 관리자 관광지 목록 페이지 */
//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/tourspot")
//    public String tourspotListPage(Model model) {
//
//        // 기본 첫 페이지(0), 정렬 기준(id), 한 페이지당 10개
//        int page = 0;
//        int size = 10;
//        String sort = "id";
//
//        // AdminService를 통해 관광지 목록 조회
//        model.addAttribute("tourspotList",
//                adminService.getAllTourSpotList(page, size, sort));
//
//        // 페이지 번호/전체 페이지 수도 추가 (페이지네이션용)
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", 1); // TODO: 나중에 실제 totalPages로 변경
//
//        // Thymeleaf 템플릿 파일 경로
//        return "admin/tourspot_list";  // → templates/admin/tourspot_list.html
//    }
//
//
//
//    @GetMapping("/tourspot/add")
//    public String tourSpotAddPage() {
//        return "admin/tourspot-add";
//    }
//}
