package com.multi.travel.ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AICourseViewController
 * @since : 2025-11-12 수요일
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/ai/courses/view")
public class AICourseViewController {

    /** AI 코스 생성 화면 */
    @GetMapping("/create")
    public String aiCourseCreate(@RequestParam Long planId, Model model) {
        model.addAttribute("planId", planId);
        return "ai/ai-course-create";
    }
}
