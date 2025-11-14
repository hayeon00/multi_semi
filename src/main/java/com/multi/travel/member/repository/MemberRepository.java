package com.multi.travel.member.repository;


import com.multi.travel.member.dto.MemberDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberDto, Long> {
    Optional<MemberDto> findByMemberEmail(String memberEmail);

    Optional<MemberDto> findByMemberId(String memberId);
}
