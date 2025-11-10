package com.multi.travel.member.service;

import com.multi.travel.member.dto.MemberReqDto;
import com.multi.travel.member.dto.MemberResDto;
import com.multi.travel.member.entity.Member;
import com.multi.travel.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : MemberService
 * @since : 2025. 11. 8. 토요일
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public List<MemberResDto> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResDto::fromEntity)
                .toList();
    }

    public MemberResDto findOne(String loginId) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() ->new IllegalArgumentException("해당회원이 존재하지 않습니다"));

        return MemberResDto.fromEntity(member);

    }

    @Transactional
    public Member update(MemberReqDto memberReqDto) {

        Member member = memberRepository.findByLoginId(memberReqDto.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        member.updateInfo(memberReqDto.getUsername(),memberReqDto.getEmail(),memberReqDto.getTel(),memberReqDto.getImage());

        return member;

    }

    public void deleteMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        member.setStatus("N");
        memberRepository.save(member);
    }



    public MemberResDto findByLoginId(String loginIdFromToken) {

        Member member = memberRepository.findByLoginId(loginIdFromToken)
                .orElseThrow(() ->new IllegalArgumentException("해당회원이 존재하지 않습니다"));

        return MemberResDto.fromEntity(member);
    }
}
