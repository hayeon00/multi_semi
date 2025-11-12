package com.multi.travel.common.jwt.service;


import com.multi.travel.common.domain.RefreshToken;
import com.multi.travel.common.exception.RefreshTokenException;
import com.multi.travel.common.jwt.TokenProvider;
import com.multi.travel.common.jwt.dto.TokenDto;
import com.multi.travel.common.jwt.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    /** JWT 토큰 생성 (Access + Refresh) */
    public <T> TokenDto createToken(T t) {
        String loginId;
        List<String> roles;
        String accessToken;
        String refreshToken;

        //  JWT 문자열에서 claims 추출
        if (t instanceof String jwt) {
            String pureToken = resolveToken(jwt);
            Claims claims = tokenProvider.parseClaimes(pureToken);
            loginId = claims.getSubject();
            String role = (String) claims.get("auth");
            roles = Arrays.asList(role.split(","));
        }
        //  Map 형태 (email + roles)일 때
        else if (t instanceof Map) {
            Map<String, Object> data = (Map<String, Object>) t;
            loginId = (String) data.get("loginId");
            roles = (List<String>) data.get("roles");
        }
        else {
            throw new IllegalArgumentException("Invalid token type !!");
        }

        //  RefreshToken 관리
        refreshToken = handleRefreshToken(loginId);

        // AccessToken 생성
        accessToken = createAccessToken(loginId, roles);

        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /** "Bearer " 접두어 제거 */
    private String resolveToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    /** RefreshToken 처리 (JPA 버전) */
    @Transactional(noRollbackFor = RefreshTokenException.class)
    public String handleRefreshToken(String loginId) {
        log.info("🔍 handleRefreshToken() 실행 중, 트랜잭션 활성 상태: {}", TransactionSynchronizationManager.isActualTransactionActive());

        Optional<RefreshToken> existingToken = refreshTokenRepository.findByLoginId(loginId);

        if (existingToken.isPresent()) {
            RefreshToken token = existingToken.get();
            LocalDateTime now = LocalDateTime.now();

            // 만료 여부 확인
            if (token.getExpiredAt().isBefore(now)) {
                refreshTokenRepository.deleteByLoginId(loginId);
                throw new RefreshTokenException("Refresh token이 만료되었습니다. 다시 로그인해주세요");
            } else {
                return token.getRefreshToken();
            }
        } else {
            String reToken = createRefreshToken(loginId);

            if (tokenProvider.validateToken(reToken)) {
                RefreshToken newToken = RefreshToken.builder()
                        .loginId(loginId)
                        .refreshToken(reToken)
                        .expiredAt(tokenProvider.getRefreshTokenExpiry())
                        .issuedAt(LocalDateTime.now())
                        .build();

                refreshTokenRepository.save(newToken);
            }
            return reToken;
        }
    }

    /** AccessToken 생성 */
    private String createAccessToken(String loginId, List<String> roles) {
        return tokenProvider.generateToken(loginId, roles, "A");
    }

    /** RefreshToken 생성 */
    private String createRefreshToken(String loginId) {
        return tokenProvider.generateToken(loginId, null, "R");
    }

    /** 로그아웃 시 RefreshToken 삭제 */
    @Transactional
    public void deleteRefreshToken(String accessToken) {
        String token = resolveToken(accessToken);
        String Id = tokenProvider.getUserId(token);
        refreshTokenRepository.deleteByLoginId(Id);
        log.info("리프레쉬 토큰 삭제 완료: {}", Id);
    }

    public String resolveTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("access_token")) {
                return cookie.getValue();
            }
        }

        return null;
    }
}