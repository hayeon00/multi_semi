package com.multi.travel.acc.controller;

/*
 * Please explain the class!!!
 *
 * @filename    : AccViewController
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 13. 목요일
 */

import com.multi.travel.common.config.AppConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/accommodations/view")
public class AccViewController {

    private final AppConfig appConfig;

    @GetMapping("/acclist")
    public String accList(
            @RequestParam("tspId") Long tspId,
            Model model) {
        model.addAttribute("tspId", tspId);
        return "/accommodation/acclist";
    }

    @GetMapping("/accdetail")
    public String accDetail(
            @RequestParam Long accId,
            @RequestParam Long tspId,
            Model model
            ) {
        model.addAttribute("accId", accId);
        model.addAttribute("tspId", tspId);
        model.addAttribute("kakaoKey", appConfig.getKakaoMapApiKey());
        return "/accommodation/accdetail";
    }
}
