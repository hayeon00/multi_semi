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

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailService customUserDetailService;
    private final TokenService tokenService;

    /** ✅ 회원가입 */
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

    /** ✅ 로그인 */
    public TokenDto login(MemberReqDto memberReqDto) {
        // 1️⃣ DB에서 회원 찾기
        Member member = memberRepository.findByLoginId(memberReqDto.getLoginId())
                .orElseThrow(() -> new BadCredentialsException("회원 정보를 찾을 수 없습니다."));

        if (member.getRole().equals("ROLE_USER") && member.getStatus().equals("N")) {
            throw new BadCredentialsException("계정이 비활성 상태입니다. 관리자에게 문의하세요");
        }

        // 2️⃣ 비밀번호 검증
        if (!passwordEncoder.matches(memberReqDto.getPassword(), member.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        // 3️⃣ Spring Security UserDetails 로부터 권한 목록 가져오기
        UserDetails userDetails = customUserDetailService.loadUserByUsername(memberReqDto.getLoginId());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // 4️⃣ AccessToken + RefreshToken 발급
        TokenDto tokenDto = tokenService.issueTokens(member.getLoginId(), roles);

        return tokenDto;
    }
}
