package com.multi.travel.plan.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/create")
    public String showCreatePage(@RequestParam(required = false) Long tspId, Model model) {
        model.addAttribute("tspId", tspId);
        return "plan/plan-create"; // templates/plan/plan-create.html
    }
}
