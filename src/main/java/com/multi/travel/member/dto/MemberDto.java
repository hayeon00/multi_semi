package com.multi.travel.member.dto;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberCode;

    @Column(name = "id", unique = true)
    private String memberId;

    @Column(nullable = false, unique = true)
    private String memberEmail;

    @Column(nullable = false)
    private String memberPassword;

    private String memberName;
    private String memberRole = "ROLE_USER";
}