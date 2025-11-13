package com.multi.travel.member.dto;

import com.multi.travel.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResDto {

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

    /**  🔹 브라우저 접근용 이미지 URL 생성 */
    public String getImageUrl() {
        if (image == null || image.isEmpty()) {
            return null;
        }
        // application.yml → image.member.url = http://localhost:8090/profile/
        return "http://localhost:8090/profile/" + image;
    }

    public static MemberResDto fromEntity(Member member) {
        return MemberResDto.builder()
                .id(member.getId())
                .loginId(member.getLoginId())
                .username(member.getUsername())
                .role(member.getRole())
                .email(member.getEmail())
                .image(member.getImage())
                .tel(member.getTel())
                .status(member.getStatus())
                .build();
    }
}