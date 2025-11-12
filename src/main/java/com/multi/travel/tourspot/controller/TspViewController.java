package com.multi.travel.tourspot.controller;

/*
 * Please explain the class!!!
 *
 * @filename    : TspViewController
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 12. 수요일
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
@RequestMapping("/spots/view")
public class TspViewController {
    @GetMapping("/tourspotlist")
    public String tourSpotList(Model model) {
        return "tourspot/tourspotlist";
    }

    @GetMapping("/tourspotdetail")
    public String tourSpotDetail(@RequestParam Long id,
                                 Model model,
                                 AppConfig appConfig) {
        model.addAttribute("id", id);
        model.addAttribute("kakaokey", appConfig.getKakaoMapApiKey());
        return "/tourspot/tourspotdetail";
    }
}
