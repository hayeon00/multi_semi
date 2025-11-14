package com.multi.travel.course.controller;

import com.multi.travel.auth.dto.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : CourseViewController
 * @since : 2025-11-12 수요일
 */
@Controller
@RequestMapping("/courses/view")
@RequiredArgsConstructor
public class CourseViewController {

    @Value("${KAKAO_MAP_API_KEY}")
    private String kakaoKey;


//    /** 코스 수정 페이지 진입 - AI 생성 코스든 일반 코스든 동일하게 사용 */
//    @GetMapping("/edit/{planId}")
//    public String editCoursePage(@PathVariable Long planId, Model model) {
//        model.addAttribute("planId", planId);
//        return "course/course-edit"; // templates/course/course-edit.html
//    }

    /** 코스 상세보기 - AI 생성 코스든 일반 코스든 동일하게 사용 */
    @GetMapping("/detail/{courseId}")
    public String showCourseDetail(
            @PathVariable Long courseId,
            @RequestParam(required = false) Long planId,
            @AuthenticationPrincipal CustomUser user,
            Model model
    ) {
        model.addAttribute("courseId", courseId);
        model.addAttribute("planId", planId);   // ⭐ 반드시 추가
        model.addAttribute("loginUserId", user != null ? user.getUserId() : null);
        model.addAttribute("kakaoKey", kakaoKey);
        return "course/course-detail";
    }

    /** 계획 생성 후 & 코스 생성 전 분기 페이지 */
    @GetMapping("/choose")
    public String showChoosePage(@RequestParam Long planId, Model model) {
        model.addAttribute("planId", planId);
        return "course/choose"; // templates/course/choose.html
    }

    /** 코스 수정(확정) 화면 이동 */
    @GetMapping("/edit")
    public String showCourseEditPage(@RequestParam Long planId, Model model) {
        model.addAttribute("planId", planId); // Thymeleaf로 전달
        model.addAttribute("kakaoKey", kakaoKey);
        return "course/course-edit";
    }

    /** 수동 코스 생성 화면 이동 */
    @GetMapping("/create/manual")
    public String showManualCreatePage(@RequestParam Long planId, Model model) {
        model.addAttribute("planId", planId);
        model.addAttribute("kakaoKey", kakaoKey);
        return "course/course-create-manual"; // templates/course/course-create-manual.html
    }

    /** 코스 목록 페이지 */
    @GetMapping("/list")
    public String showCourseList(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(required = false) Long planId,
            Model model
    ) {
        model.addAttribute("planId", planId);
        model.addAttribute("loginUserId", user != null ? user.getUserId() : null);
        return "course/course-list"; // templates/course/course-list.html
    }

}