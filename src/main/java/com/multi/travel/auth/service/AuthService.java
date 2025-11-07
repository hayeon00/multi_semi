package com.multi.travel.auth.service;


import com.multi.travel.common.exception.DuplicateUsernameException;
import com.multi.travel.common.jwt.dto.TokenDto;
import com.multi.travel.common.jwt.service.TokenService;
import com.multi.travel.member.dto.MemberDto;
import com.multi.travel.member.dto.MemberReqDto;
import com.multi.travel.member.dto.MemberResDto;
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
    public MemberResDto signup(MemberReqDto memberReqDto) {
        if (memberRepository.findByMemberEmail(memberReqDto.getMemberEmail()).isPresent()) {
            throw new DuplicateUsernameException("이메일이 중복됩니다");
        }

        MemberDto member = MemberDto.builder()
                .memberId(memberReqDto.getMemberId())
                .memberEmail(memberReqDto.getMemberEmail())
                .memberPassword(passwordEncoder.encode(memberReqDto.getMemberPassword()))
                .memberName(memberReqDto.getMemberName())
                .memberRole("ROLE_USER")
                .build();

        memberRepository.save(member);

        return MemberResDto.builder()
                .memberId(member.getMemberId())
                .memberCode(member.getMemberCode())
                .memberEmail(member.getMemberEmail())
                .memberName(member.getMemberName())
                .memberRole(member.getMemberRole())
                .build();
    }

    /** 로그인 */
    public TokenDto login(MemberReqDto memberReqDto) {
        // 1️⃣ DB에서 회원 찾기
        MemberDto member = memberRepository.findByMemberId(memberReqDto.getMemberId())
                .orElseThrow(() -> new BadCredentialsException("회원 정보를 찾을 수 없습니다."));


        // 2️⃣ 비밀번호 검증
        if (!passwordEncoder.matches(memberReqDto.getMemberPassword(), member.getMemberPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        // 3️⃣ 인증 객체 생성
        UserDetails userDetails = customUserDetailService.loadUserByUsername(memberReqDto.getMemberEmail());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // 4️⃣ 토큰 발급
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("email", memberReqDto.getMemberEmail());
        loginData.put("roles", roles);

        return tokenService.createToken(loginData);
    }
}
