package com.multi.travel.auth.service;


import com.multi.travel.common.exception.DuplicateUsernameException;
import com.multi.travel.common.jwt.dto.TokenDto;
import com.multi.travel.common.jwt.service.TokenService;
import com.multi.travel.member.dto.MemberReqDto;
import com.multi.travel.member.entity.Member;
import com.multi.travel.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailService customUserDetailService;
    private final TokenService tokenService;

    /** 회원가입 */
    @Transactional
    public Member signup(MemberReqDto memberReqDto) {
        if (memberRepository.findByLoginId(memberReqDto.getLoginId()).isPresent()) {
            throw new DuplicateUsernameException("아이디가 중복됩니다");
        }

        Member member = Member.builder()
                .loginId(memberReqDto.getLoginId())
                .email(memberReqDto.getEmail())
                .password(passwordEncoder.encode(memberReqDto.getPassword()))
                .username(memberReqDto.getUsername())
                .role("ROLE_USER")
                .tel(memberReqDto.getTel())
                .status("Y")
                .build();

        memberRepository.save(member);

        return member;
    }

    /** 로그인 */
    public TokenDto login(MemberReqDto memberReqDto) {
        //  DB에서 회원 찾기
        Member member = memberRepository.findByLoginId(memberReqDto.getLoginId())
                .orElseThrow(() -> new BadCredentialsException("회원 정보를 찾을 수 없습니다."));


        // 비밀번호 검증
        if (!passwordEncoder.matches(memberReqDto.getPassword(), member.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        // 인증 객체 생성
        UserDetails userDetails = customUserDetailService.loadUserByUsername(memberReqDto.getLoginId());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // 토큰 발급
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("loginId", memberReqDto.getLoginId());
        loginData.put("roles", roles);

        return tokenService.createToken(loginData);
    }


}
