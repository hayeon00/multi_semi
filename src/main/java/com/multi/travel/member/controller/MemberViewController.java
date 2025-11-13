package com.multi.travel.member.controller;

import com.multi.travel.common.jwt.TokenProvider;
import com.multi.travel.common.jwt.service.TokenService;
import com.multi.travel.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : chang
 * @filename : MemberViewController
 * @since : 2025-11-10 월요일
 */

@Controller
@RequestMapping("/member/view")
@RequiredArgsConstructor
@Slf4j
public class MemberViewController {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;
    private final TokenService tokenService;

    /**
     * ✅ 회원정보 수정 페이지 (GET)
     * - 쿠키에서 AccessToken 추출
     * - 로그인된 회원 정보 조회 후 모델에 전달
     * - templates/member/edit.html 렌더링
     */


    /** ✅ 마이페이지 화면 렌더링 */
    @GetMapping("/mypage")
    public String myPage() {
        return "member/mypage"; //
    }

    @GetMapping("/edit")
    public String showEditPage() {
        log.info("[View] GET /member/view/edit - 회원정보 수정 페이지 요청");
        return "member/edit"; // templates/member/edit.html
    }


}
