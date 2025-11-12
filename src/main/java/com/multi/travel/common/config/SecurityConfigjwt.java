package com.multi.travel.common.config;

import com.multi.travel.common.jwt.JwtAcessDeniedHandler;
import com.multi.travel.common.jwt.JwtAuthenticationEntrypoint;
import com.multi.travel.common.jwt.JwtFilter;
import com.multi.travel.common.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

                .authorizeHttpRequests(auth -> auth
                        // ✅ 정적 리소스는 로그인 없이 접근 허용
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/fonts/**",
                                "/static/**"
                        ).permitAll()

                        // ✅ 로그인/회원가입/토큰 관련 경로 허용
                        .requestMatchers(
                                "/login",
                                "/signup",
                                "/auth/**",
                                "/api/auth/**"
                        ).permitAll()

                        // ✅ 관리자 뷰 페이지(Thymeleaf HTML)는 로그인 없이 접근 허용
                        //   (AccessToken 만료 시에도 페이지가 열리도록)
                        .requestMatchers("/admin/view/**").permitAll()

                        // ✅ 나머지 모든 요청(API)은 JWT 인증 필요
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
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8090:*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));
        configuration.setAllowCredentials(true); // ✅ 쿠키 전송 허용 (핵심)
        configuration.addExposedHeader("Set-Cookie");
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
