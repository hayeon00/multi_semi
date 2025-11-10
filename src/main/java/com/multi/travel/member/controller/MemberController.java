package com.multi.travel.member.controller;

import com.multi.travel.common.ResponseDto;
import com.multi.travel.common.jwt.TokenProvider;
import com.multi.travel.common.jwt.service.TokenService;
import com.multi.travel.member.dto.MemberReqDto;
import com.multi.travel.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : MemberController
 * @since : 2025. 11. 8. 토요일
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;
    private final TokenService tokenService;


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ResponseDto> getMembers() {
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK,"전체회원조회 성공",memberService.findAll())

        );
    }

    @GetMapping("/{loginId}")
    public ResponseEntity<ResponseDto> getOneMember(@PathVariable String loginId) {
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK,"회원상세조회 성공",memberService.findOne(loginId))

        );
    }

    //회원정보 수정
    @PutMapping("/update")
    public ResponseEntity<ResponseDto> updateMember(@RequestBody MemberReqDto memberReqDto, HttpServletRequest request) {

        // 쿠키에서 access_token 추출
        String accessToken = tokenService.resolveTokenFromCookies(request);
        if (accessToken == null) {
            throw new AccessDeniedException("AccessToken이 존재하지 않습니다.");
        }
        //  AccessToken 검증
        if (!tokenProvider.validateToken(accessToken)) {
            throw new AccessDeniedException("유효하지 않거나 만료된 토큰입니다.");
        }

        String loginIdFromToken = tokenProvider.getUserId(accessToken);

        if (!loginIdFromToken.equals(memberReqDto.getLoginId())) {
            throw new AccessDeniedException("본인 정보만 수정할 수 있습니다.");
        }

        memberService.update(memberReqDto);

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "회원정보 수정 성공", null));

    }
    // 🔹 회원 삭제
    @PreAuthorize("hasRole('ADMIN')")  // ✅ 관리자만 가능
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "회원 삭제 성공", null)
        );
    }

}
