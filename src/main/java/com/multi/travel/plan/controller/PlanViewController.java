package com.multi.travel.plan.controller;

import org.springframework.beans.factory.annotation.Value;
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
 * @filename : PlanViewController
 * @since : 2025-11-12 수요일
 */
@Controller
@RequestMapping("/plans/view")
public class PlanViewController {

    @Value("${KAKAO_MAP_API_KEY}")
    private String kakaoKey;

    @GetMapping("/create")
    public String showCreatePage(@RequestParam(required = false) Long tspId, Model model) {
        model.addAttribute("tspId", tspId);
        return "plan/plan-create"; // templates/plan/plan-create.html
    }

    @GetMapping("/manual")
    public String showManualPage(@RequestParam Long planId, Model model) {
        model.addAttribute("planId", planId);
        return "course/course-create-manual";
    }

    @GetMapping("/{planId}")
    public String showPlanDetailPage(@PathVariable Long planId, Model model) {
        model.addAttribute("planId", planId);
        model.addAttribute("kakaoKey", kakaoKey);
        return "plan/plan-detail";
    }

    @GetMapping("/edit")
    public String showEditPage(@RequestParam Long planId, Model model) {
        model.addAttribute("planId", planId);
        model.addAttribute("kakaoKey", kakaoKey);
        return "plan/plan-edit";
    }

}
