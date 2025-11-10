package com.multi.travel.member.repository;


import com.multi.travel.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {


    Optional<Member> findByLoginId(String loginId);

}
