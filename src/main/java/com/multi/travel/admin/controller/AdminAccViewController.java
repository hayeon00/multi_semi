package com.multi.travel.admin.controller;

/*
 * Please explain the class!!!
 *
 * @filename    : AdminAccViewController
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 14. 금요일
 */


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/view/acc")
@RequiredArgsConstructor
@Slf4j
public class AdminAccViewController {
    /**
     * 관리자 관광지 목록 페이지
     */
    @GetMapping
    public String accListPage(
    ) {
        return "admin/acc_list";
    }


    /**
     * 관리자 관광지 추가 페이지
     */
    @GetMapping("/add")
    public String tourSpotAddPage() {
        return "admin/acc-add";
    }

    @GetMapping("/edit/{id}")
    public String editTourSpotView(@PathVariable Long id, Model model) {
        model.addAttribute("id", id);
        return "admin/acc_edit";
    }
}
