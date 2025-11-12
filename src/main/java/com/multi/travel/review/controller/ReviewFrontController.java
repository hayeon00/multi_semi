package com.multi.travel.review.controller;

import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.review.dto.ReviewDetailDto;
import com.multi.travel.review.service.ReviewService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : hayeon
 * @filename : ReviewPageController
 * @since : 2025. 11. 12. 수요일
 */
@Controller
@RequestMapping("/review/view")
public class ReviewFrontController {

    ReviewService reviewService;

    @GetMapping("/regist")
    public String showReviewForm(Model model, @AuthenticationPrincipal CustomUser user) {
        model.addAttribute("user", user);
        return "review/course/reviewForm";
    }

    @GetMapping("/detail")
    public String getReviewDetail(@PathVariable Long id, Model model) {
        ReviewDetailDto review = reviewService.getReviewById(id);
        model.addAttribute("review", review);
        return "review/course/detail";
    }



}