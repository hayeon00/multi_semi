package com.multi.travel.tourspot.controller;

/*
 * Please explain the class!!!
 *
 * @filename    : RootController
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 13. 목요일
 */


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @GetMapping({"/", "/home", "/main"})
    public String index() {
        return "redirect:/spots/view/tourspotlist";
    }
}
