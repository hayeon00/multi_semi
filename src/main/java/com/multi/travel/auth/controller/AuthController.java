package com.multi.travel.auth.controller;


import com.multi.travel.auth.service.AuthService;
import com.multi.travel.common.ResponseDto;
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
@RequestMapping("auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    //가입
    @PostMapping("/signup")
    public ResponseEntity<ResponseDto> signup(@RequestBody MemberReqDto memberReqDto) {

        return ResponseEntity.ok(new ResponseDto(HttpStatus.CREATED, "회원가입 성공", authService.signup(memberReqDto)));

    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto> login(@RequestBody MemberReqDto memberReqDto,
                                             HttpServletResponse response) {

        TokenDto token = authService.login(memberReqDto);

        // ✅ AccessToken 쿠키
        Cookie accessCookie = new Cookie("access_token", token.getAccessToken());
        accessCookie.setHttpOnly(true);  // 자바스크립트 접근 불가 (보안)
        accessCookie.setSecure(false);    // HTTPS에서만 전송
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 3);  // 3분

        // ✅ RefreshToken 쿠키
        Cookie refreshCookie = new Cookie("refresh_token", token.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24); // 1일

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "로그인 성공", null));
    }



    @PostMapping("/refresh")
    public ResponseEntity<ResponseDto> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;

        // ✅ 쿠키에서 refresh_token 찾기
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }


        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDto(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 없습니다", null));
        }

        TokenDto tokenDto = tokenService.createToken(refreshToken);

        // ✅ 새 access_token 쿠키 갱신
        Cookie accessCookie = new Cookie("access_token", tokenDto.getAccessToken());
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 3);  // 3분
        response.addCookie(accessCookie);

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "AccessToken 재발급 성공", null));
    }


    @PostMapping("/logout")
    public ResponseEntity<ResponseDto> logout(HttpServletResponse response) {
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





}
