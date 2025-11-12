package com.multi.travel.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberReqDto {

    /** 회원 PK (수정 시 필요) */
    private Long id;

    /** 로그인용 아이디 */
    private String loginId;

    /** 회원 이름 */
    private String username;

    /** 비밀번호 */
    private String password;

    /** 이메일 */
    private String email;

    /** 역할 (기본값 ROLE_USER) */
    private String role;

    /** 상태 (Y: 활성, N: 비활성) */
    private String status;

    /** 전화번호 */
    private String tel;

    /** 프로필 이미지 */
    private String image;

    private MultipartFile imageFile;
}