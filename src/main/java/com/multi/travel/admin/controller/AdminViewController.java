package com.multi.travel.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : chang
 * @filename : AdminViewController
 * @since : 2025-11-09 일요일
 */
@Controller
@RequestMapping("/admin/view")
public class AdminViewController {


    @GetMapping("/members")
    public String memberListPage() {
        return "admin/member-list";
    }


}
