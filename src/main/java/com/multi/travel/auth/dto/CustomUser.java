package com.multi.travel.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomUser implements UserDetails {
    private String userId;
    private String email;
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        // Spring Security에서 사용하는 username은 우리 시스템의 userId와 동일하게 처리
        return getUserId();
    }


    public String getUserId() {
        return userId;
    } // 아이디르 반환하여 Spring Security에서 로그인 ID로 사용

    @Override
    public boolean isAccountNonExpired() {
        return true; }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}