package com.multi.travel.common.jwt;


import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.common.exception.TokenException;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";  // 클레임에서 권한정보담을키
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 3;     // 60분
    private static final long REFRESH_TOKEN_EXPIRE_TIME =1000L * 60 * 60 * 24; //1000L * 60 * 60 * 24 * 1;  // 1일

    private final JwtProvider jwtProvider;  // JwtProvider 의존성 추가
    private final Key SKEY;
    private final String ISSUER;

    //application.yml 에 정의해놓은 jwt.secret 값을 가져와서 JWT 를 만들 때 사용하는 암호화 키값을 생성
    public TokenProvider(JwtProvider jwtProvider) {

        this.jwtProvider = jwtProvider;
        SKEY = jwtProvider.getSecretKey();
        ISSUER = jwtProvider.getIssuer();
        System.out.println("TokenProvider-------------"+ SKEY);
        System.out.println("   ISSUER     -------------"+ ISSUER);

    }

    public String generateToken(String loginId, List<String> roles, String code) {

        Claims claims = Jwts
                .claims()
                .setSubject(loginId);

        // ✅ 두 토큰 모두에 auth 클레임 추가 (역할정보 유지)
        claims.put(AUTHORITIES_KEY, String.join(",", roles));

        long now = (new Date()).getTime();
        Date tokenExpiresIn = new Date();
        if (code.equals("A")) {  //액세스 토큰
            tokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
            claims.put(AUTHORITIES_KEY, String.join(",", roles)); // List<String> -> 콤마로 구분된 문자열
        } else {  //리프레시 토큰
            tokenExpiresIn = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);
        }



        return Jwts.builder()
                .setIssuer(ISSUER) // 프로퍼티에 설정한 발행자
                .setIssuedAt(new Date(now)) // 발생일 , 현재 시간
                .setClaims(claims)                        // payload "auth": "ROLE_USER"
                .setExpiration(tokenExpiresIn)       // payload "exp": 1516239022 (예시) // exp : Expiration Time. 토큰 만료 시각
                .signWith(SKEY, SignatureAlgorithm.HS512)   // header "alg": "HS512"  // "alg": "서명 시 사용하는 알고리즘",
                .compact();


    }

    public LocalDateTime getRefreshTokenExpiry() {

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime reTokenExpiry = now.plus(REFRESH_TOKEN_EXPIRE_TIME, ChronoUnit.MILLIS);

        return reTokenExpiry;

    }

    public boolean validateToken(String token) {
        try {
            log.info("[TokenProvider] 유효성 검증 중인 토큰: {}", token);
            // 토큰을 비밀 키 와함께 복호화를 진행 해서 유효하지 않으면 false 반환, 유효하면 true 반환
            Jwts.parserBuilder()
                    .setSigningKey(SKEY)
                    .build()
                    .parseClaimsJws(token);

            log.info("[TokenProvider] JWT 토큰이 유효합니다.");
            return true;

        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("[TokenProvider] 잘못된 JWT 서명입니다. 토큰: {}", token, e);
            throw new TokenException("잘못된 JWT 서명입니다.");

        } catch (ExpiredJwtException e) {
            log.warn("[TokenProvider] 만료된 JWT 토큰입니다. 토큰: {}, 만료 시각: {}", token, e.getClaims().getExpiration(), e);
            throw new TokenException("만료된 JWT 토큰입니다.");

        } catch (UnsupportedJwtException e) {
            log.error("[TokenProvider] 지원되지 않는 JWT 토큰입니다. 토큰: {}", token, e);
            throw new TokenException("지원되지 않는 JWT 토큰입니다.");

        } catch (IllegalArgumentException e) {
            log.error("[TokenProvider] JWT 토큰이 잘못되었습니다. 토큰: {}", token, e);
            throw new TokenException("JWT 토큰이 잘못되었습니다.");
        }
    }

    public Authentication getAuthentication(String jwt) {

        Claims claims = parseClaimes(jwt);
        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한정보가 없는 토큰입니다");

        }
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        log.info("[TokenProvider] authorities : {}", authorities);

        CustomUser customUser = new CustomUser();
        customUser.setUserId(claims.getSubject()); // 로그인 ID(loginId) -> 기존 코드(claims.getId())의 문제: 실제 JWT에는 id 필드가 없음
        customUser.setEmail((String) claims.get("email")); // 이메일 claim이 있을 때만 -> 기존 코드(claims.getSubject())의 문제: subject에 로그인 ID가 들어 있음
        customUser.setAuthorities(authorities);
        return new UsernamePasswordAuthenticationToken(customUser, "", authorities);
    }

    public Claims parseClaimes(String jwt) {

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SKEY).build().parseClaimsJws(jwt).getBody();
        }catch (ExpiredJwtException e){
            return e.getClaims();
        }
    }

    public String getUserId(String accessToken) {

        return Jwts
                .parserBuilder()
                .setSigningKey(SKEY)
                .build()
                .parseClaimsJws(accessToken).getBody().getSubject();
    }
}
