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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationEntrypoint jwtAuthenticationEntrypoint) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))


                .authorizeHttpRequests(auth->auth

                        .requestMatchers("/login", "/signup", "/css/**", "/images/**").permitAll()
                        .requestMatchers("/auth/**", "/api/auth/**").permitAll()
                        .requestMatchers("/**").permitAll() // /api/plans, /api/courses 요청 시 로그인 없이도 테스트 가능하게 설정
                                                                  // TODO: 전체 구현 완료 시 삭제 예정



                        .anyRequest().authenticated())


                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(exception->exception
                        .authenticationEntryPoint(jwtAuthenticationEntrypoint)
                        .accessDeniedHandler(jwtAcessDeniedHandler)
                );


        return http.build();

    }
    // ✅ CORS 설정 추가 (쿠키 기반 통신 허용)
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
