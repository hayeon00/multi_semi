package com.multi.travel.common.config;


import com.multi.travel.common.jwt.JwtAcessDeniedHandler;
import com.multi.travel.common.jwt.JwtAuthenticationEntrypoint;
import com.multi.travel.common.jwt.JwtFilter;
import com.multi.travel.common.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfigjwt {

    private final TokenProvider tokenProvider;
    private final JwtAcessDeniedHandler jwtAcessDeniedHandler;
    private final JwtFilter jwtFilter;



    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationEntrypoint jwtAuthenticationEntrypoint
    ) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                )
                .logout(AbstractHttpConfigurer::disable)
                .securityContext((context) -> context.requireExplicitSave(false))

                .authorizeHttpRequests(auth -> auth
                        // ========================================
                        // 1. 정적 리소스
                        // ========================================
                        .requestMatchers(
                                "/",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/fonts/**",
                                "/static/**",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()

                        // ========================================
                        // 2. 인증 관련
                        // ========================================
                        .requestMatchers("/auth/**", "/signup", "/login").permitAll()

                        // ========================================
                        // 3. View 경로 (페이지 렌더링)
                        // ========================================

                        // 3-1. 공개 View 페이지 (로그인 불필요)
                        .requestMatchers(
                                "/spots/view/**",           // 관광지 목록, 상세
                                "/accommodations/view/**",  // 숙박 목록, 상세
                                "/courses/view/**",         // 코스 목록, 상세
                                "/reviews/view/**"          // 리뷰 목록, 상세
                        ).permitAll()

                        // 3-2. 인증 필요 View 페이지 (로그인 필수)
                        .requestMatchers(
                                "/members/view/mypage",     // 마이페이지
                                "/members/view/update",     // 회원정보 수정 페이지
                                "/plans/view/**"            // 여행 계획 페이지
                        ).authenticated()

                        // 3-3. 관리자 View 페이지
                        .requestMatchers("/admin/view/**").hasRole("ADMIN")

                        // ========================================
                        // 4. API - GET 요청 (공개 조회)
                        // ========================================
                        .requestMatchers(HttpMethod.GET,
                                "/",
                                "/categories/**",
                                "/spots/**",              // ✅ /spots/detail 포함
                                "/api/**",
                                "/accommodations/**",
                                "/reviews/**",
                                "/courses/**"
                        ).permitAll()

                        // ========================================
                        // 5. API - POST 요청 (인증 필요)
                        // ========================================
                        .requestMatchers(HttpMethod.POST,
                                "/courses/**",
                                "/plans/**",
                                "/reviews/**",
                                "/accommodations/**",
                                "/recommend/**"
                        ).hasAnyRole("USER", "ADMIN")

                        // ========================================
                        // 6. API - PUT 요청 (인증 필요)
                        // ========================================
                        .requestMatchers(HttpMethod.PUT,
                                "/courses/**",
                                "/plans/**",
                                "/reviews/**",
                                "/members/**",
                                "/accommodations/**"
                        ).hasAnyRole("USER", "ADMIN")

                        // ========================================
                        // 7. API - DELETE 요청 (인증 필요)
                        // ========================================
                        .requestMatchers(HttpMethod.DELETE,
                                "/spots/**",
                                "/courses/**",
                                "/plans/**",
                                "/reviews/**",
                                "/members/**",
                                "/accommodations/**"
                        ).hasAnyRole("USER", "ADMIN")

                        // ========================================
                        // 8. Members API (모든 메서드 인증 필요)
                        // ========================================
                        .requestMatchers("/members/**").hasAnyRole("USER", "ADMIN")

                        // ========================================
                        // 9. 관리자 전용
                        // ========================================
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // ========================================
                        // 10. 나머지
                        // ========================================
                        .anyRequest().authenticated()
                )

                // ✅ JWT 필터 추가
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // ✅ 인증 실패/권한 거부 처리
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntrypoint)
                        .accessDeniedHandler(jwtAcessDeniedHandler)
                );


        return http.build();

    }

    // ✅ CORS 설정 (쿠키 전송 허용)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8090")); // 프론트 포트 (같으면 그대로)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));
        configuration.setAllowCredentials(true); // ✅ 쿠키 전송 허용 (핵심)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}
