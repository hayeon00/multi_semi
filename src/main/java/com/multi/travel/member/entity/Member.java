package com.multi.travel.member.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Please explain the class!!!
 *
 * @author : chang
 * @filename : Member
 * @since : 2025-11-07 금요일
 */

@Entity
@Table(name = "tb_usr")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Slf4j
@Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "login_id",nullable = false,unique = true,length = 100)
    private String loginId;

    @Column(name = "username",nullable = false,length = 100)
    private String username;

    @Column(name = "password",nullable = false,length = 100)
    private String password;

    @Column(name = "email",nullable = false,length = 100)
    private String email;

    @Column(name = "role",columnDefinition = "ENUM('ROLE_USER','ROLE_ADMIN') DEFAULT 'ROLE_USER'")
    private String role;

    @Column (name = "status",columnDefinition = "CHAR(1) CHECK (status IN ('Y','N')) DEFAULT 'Y'")
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Column(name = "tel", length = 100,nullable = false)
    private String tel;

    @Column(name = "image", length = 100, columnDefinition = "VARCHAR(100) DEFAULT 'default.img'")
    private String image;

    public void updateInfo(String username, String email, String tel) {
        log.info("updateInfo 호출 - 전달받은 값: username={}, email={}, tel={}", username, email, tel);

        if (username != null && !username.isEmpty()) {
            this.username = username;
            log.info("username 변경: {}", this.username);
        }
        if (email != null && !email.isEmpty()) {
            this.email = email;
            log.info("email 변경: {}", this.email);
        }
        if (tel != null && !tel.isEmpty()) {
            this.tel = tel;
            log.info("tel 변경: {}", this.tel);
        }
    }




    public void setStatus(String status) {
        this.status = status;
    }


    // ✅ ID 기반 equals/hashCode 정의
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        Member member = (Member) o;
        return id != null && id.equals(member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }




}
