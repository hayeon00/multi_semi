package com.multi.travel.auth.service;


import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.member.entity.Member;
import com.multi.travel.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId));

        return CustomUser.builder()
                .userId(member.getLoginId())
                .email(member.getEmail())
                .password(member.getPassword())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority(member.getRole())
                ))
                .build();
    }
}
