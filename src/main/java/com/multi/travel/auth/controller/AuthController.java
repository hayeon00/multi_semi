package com.multi.travel.auth.controller;

import com.multi.travel.auth.service.AuthService;
import com.multi.travel.common.ResponseDto;
import com.multi.travel.common.jwt.TokenProvider;
import com.multi.travel.common.jwt.dto.TokenDto;
import com.multi.travel.common.jwt.service.TokenService;
import com.multi.travel.member.dto.MemberReqDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final TokenProvider tokenProvider;

    /**  회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<ResponseDto> signup(@RequestBody MemberReqDto memberReqDto) {
        ResponseDto response = new ResponseDto(HttpStatus.CREATED, "회원가입 성공", authService.signup(memberReqDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**  로그인 */
    @PostMapping("/login")
    public ResponseEntity<ResponseDto> login(@RequestBody MemberReqDto memberReqDto,
                                             HttpServletResponse response) {

        // ✅ AuthService 내부에서 사용자 인증 수행 (아이디/비밀번호 검증 + 회원 조회)
        TokenDto token = authService.login(memberReqDto);   // 🔹 AuthService 내부에서 issueTokens() 호출함

        // ✅ AccessToken 쿠키
        Cookie accessCookie = new Cookie("access_token", token.getAccessToken());
        accessCookie.setHttpOnly(true);   // 자바스크립트 접근 차단
        accessCookie.setSecure(false);    // HTTPS 환경이라면 true로 변경
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 60*3);  // 30분

        // ✅ RefreshToken 쿠키
        Cookie refreshCookie = new Cookie("refresh_token", token.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24);  // 1일

        // ✅ 쿠키 추가
        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "로그인 성공", null));
    }


    /** ✅ AccessToken 재발급 (RefreshToken 사용) */
    @PostMapping("/refresh")
    public ResponseEntity<ResponseDto> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookies(request);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDto(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 없습니다", null));
        }

        // ✅ TokenService에서 AccessToken 재발급
        TokenDto tokenDto = tokenService.refreshAccessToken(refreshToken);

        // ✅ 새 AccessToken 쿠키 갱신
        Cookie accessCookie = new Cookie("access_token", tokenDto.getAccessToken());
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 60*3); // 30분
        response.addCookie(accessCookie);

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "AccessToken 재발급 성공", null));
    }

    /** ✅ 로그아웃 */
    @PostMapping("/logout")
    public ResponseEntity<ResponseDto> logout(HttpServletResponse response, HttpServletRequest request) {


        String accessToken = tokenService.resolveTokenFromCookies(request);

        if (accessToken != null) {
            tokenService.deleteRefreshToken(accessToken);
        }

        // ✅ 쿠키 만료 처리
        Cookie accessCookie = new Cookie("access_token", null);
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");

        Cookie refreshCookie = new Cookie("refresh_token", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "로그아웃 성공", null));
    }

    /** ✅ 내부 유틸: 쿠키에서 RefreshToken 추출 */
    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("refresh_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
