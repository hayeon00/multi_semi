package com.multi.travel.ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AICourseViewController
 * @since : 2025-11-12 수요일
 */
@Controller
@RequiredArgsConstructor
public class AICourseViewController {

    /** AI 코스 결과 페이지 */
    @GetMapping("/ai/courses/view/{planId}")
    public String showAICourseView(@PathVariable Long planId, Model model) {
        model.addAttribute("planId", planId);
        return "ai/course-view"; // → 결과 페이지
    }

    /** 피드백 입력 페이지 */
    @GetMapping("/ai/courses/feedback/{planId}")
    public String showFeedbackPage(@PathVariable Long planId, Model model) {
        model.addAttribute("planId", planId);
        return "ai/course-feedback"; // → 피드백 입력 페이지
    }
}
