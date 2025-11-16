package com.multi.travel.member.controller;

import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.common.ResponseDto;
import com.multi.travel.common.jwt.TokenProvider;
import com.multi.travel.common.jwt.service.TokenService;
import com.multi.travel.member.dto.MemberReqDto;
import com.multi.travel.member.dto.MemberResDto;
import com.multi.travel.member.service.MemberService;
import com.multi.travel.plan.dto.PlanReqDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : MemberController
 * @since : 2025. 11. 8. 토요일
 */
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;
    private final TokenService tokenService;



    @GetMapping("/{loginId}")
    public ResponseEntity<ResponseDto> getOneMember(@PathVariable String loginId) {
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK,"회원상세조회 성공",memberService.findOne(loginId))

        );
    }
    /**
     * ✅ 로그인된 회원정보 조회 (SecurityContext 기반)
     * - JWTFilter를 통해 인증이 끝나면 SecurityContextHolder에 CustomUser가 들어감
     * - @AuthenticationPrincipal 로 현재 로그인한 사용자 정보 접근 가능
     */
    @GetMapping("/info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto> getMemberInfo(@AuthenticationPrincipal CustomUser user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDto(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.", null));
        }

        log.info("[GET /members/info] 로그인 사용자: {}", user.getUserId());
        MemberResDto member = memberService.findByLoginId(user.getUserId());

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "회원정보 조회 성공", member));
    }





    // 회원 삭제 (본인만 가능)
    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDto> deleteMyAccount(HttpServletRequest request) {


        String accessToken = tokenService.resolveTokenFromCookies(request);
        if (accessToken == null) {
            throw new AccessDeniedException("AccessToken이 존재하지 않습니다.");
        }


        if (!tokenProvider.validateToken(accessToken)) {
            throw new AccessDeniedException("유효하지 않거나 만료된 토큰입니다.");
        }


        String loginIdFromToken = tokenProvider.getUserId(accessToken);


        MemberResDto member = memberService.findByLoginId(loginIdFromToken);


        memberService.deleteMember(member.getId());

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "본인 계정 삭제 성공", null)
        );
    }

    @GetMapping("/plans")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto> getMyPlans(@AuthenticationPrincipal CustomUser user) {
        String loginId = user.getUserId(); // ✅ CustomUser에서 꺼냄
        List<PlanReqDto> plans = memberService.getMyTripPlans(loginId);
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "내 여행계획 전체조회 성공", plans)
        );
    }

    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mypage")
    public ResponseEntity<ResponseDto> getMyPageInfo(@AuthenticationPrincipal CustomUser user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDto(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.", null));
        }

        log.info("[GET /member/mypage] 로그인 사용자: {}", user.getUserId());
        MemberResDto member = memberService.findByLoginId(user.getUserId());

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "회원 정보 조회 성공", member)
        );
    }

    @PutMapping(value = "/update", consumes = {"multipart/form-data"})
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<ResponseDto> updateMember(
            @RequestPart("dto") MemberReqDto dto,                           // ✅ JSON 데이터
            @RequestPart(value = "file", required = false) MultipartFile file,  // ✅ 파일 분리
            @AuthenticationPrincipal CustomUser user
    ) {
        String loginId = user.getUserId(); // 로그인한 아이디
        log.info("[MemberController] 회원정보 수정 요청: loginId={}, dto={}", loginId, dto);

        // ✅ Service에 dto와 file을 분리해서 전달
        memberService.updateMember(loginId, dto, file);

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "회원정보가 수정되었습니다.", null)
        );
    }


}
