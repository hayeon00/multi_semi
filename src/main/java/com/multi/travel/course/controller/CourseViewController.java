package com.multi.travel.course.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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

    /** 코스 수정 페이지 진입 - AI 생성 코스든 일반 코스든 동일하게 사용 */
    @GetMapping("/edit/{planId}")
    public String editCoursePage(@PathVariable Long planId, Model model) {
        model.addAttribute("planId", planId);
        return "course/course-edit"; // templates/course/course-edit.html
    }

    /** 코스 상세보기 - AI 생성 코스든 일반 코스든 동일하게 사용 */
    @GetMapping("/courses/view/{planId}")
    public String viewCoursePage(@PathVariable Long planId, Model model) {
        model.addAttribute("planId", planId);
        return "course/course-view";
    }
}