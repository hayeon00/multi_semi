package com.multi.travel.member.controller;

import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.common.jwt.TokenProvider;
import com.multi.travel.common.jwt.service.TokenService;
import com.multi.travel.member.dto.MemberResDto;
import com.multi.travel.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
@RequestMapping("/member")
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
    @GetMapping("/edit")
    public String showEditPage(HttpServletRequest request, Model model) {

        // 1️⃣ 쿠키에서 토큰 추출
        String accessToken = tokenService.resolveTokenFromCookies(request);
        if (accessToken == null) {
            throw new AccessDeniedException("AccessToken이 존재하지 않습니다.");
        }

        // 2️⃣ 토큰 유효성 검사
        if (!tokenProvider.validateToken(accessToken)) {
            throw new AccessDeniedException("유효하지 않거나 만료된 토큰입니다.");
        }

        // 3️⃣ 토큰에서 사용자 loginId 추출
        String loginIdFromToken = tokenProvider.getUserId(accessToken);
        log.info("[GET /member/edit] 로그인 사용자: {}", loginIdFromToken);

        // 4️⃣ 해당 회원 정보 조회
        MemberResDto member = memberService.findByLoginId(loginIdFromToken);

        // 5️⃣ 모델에 회원 정보 담아서 Thymeleaf 페이지 렌더링
        model.addAttribute("member", member);

        return "member/edit";  // templates/member/edit.html
    }

    /**
     * ✅ 로그인한 회원의 마이페이지로 이동
     * - @AuthenticationPrincipal 로 로그인 정보(CustomUser) 받아오기
     * - MemberService 로 DB에서 상세 정보 조회 후 Model에 담기
     * - mypage.html View로 이동
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mypage")
    public String myPage(@AuthenticationPrincipal CustomUser user, Model model) {

        // 로그인 정보 확인
        log.info("[MemberViewController] 로그인 사용자: {}", user.getUserId());

        // DB에서 회원 상세 조회
        MemberResDto member = memberService.findByLoginId(user.getUserId());

        // View에 전달
        model.addAttribute("member", member);

        // templates/mypage.html 로 이동
        return "mypage";
    }


}
