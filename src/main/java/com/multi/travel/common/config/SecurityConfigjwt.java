package com.multi.travel.common.config;


import com.multi.travel.common.jwt.JwtAcessDeniedHandler;
import com.multi.travel.common.jwt.JwtAuthenticationEntrypoint;
import com.multi.travel.common.jwt.JwtFilter;
import com.multi.travel.common.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
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
              //  .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))


                .authorizeHttpRequests(auth->auth
                        .requestMatchers("/api/**").permitAll() // /api/plans, /api/courses 요청 시 로그인 없이도 테스트 가능하게 설정
                                                                  // TODO: 전체 구현 완료 시 삭제 예정
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/v1/products/**").permitAll()
                        .requestMatchers("/api/v1/reviews/**").hasAnyRole("ADMIN","USER")
                        .requestMatchers("/api/v1/products-management/**").hasAnyRole("ADMIN")
                        .requestMatchers("/api/**").hasAnyRole("ADMIN","USER")
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        .anyRequest().authenticated())


                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(exception->exception
                        .authenticationEntryPoint(jwtAuthenticationEntrypoint)
                        .accessDeniedHandler(jwtAcessDeniedHandler)
                );


        return http.build();

    }
//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // 모든포트허용 *
//        configuration.setAllowedMethods(Arrays.asList("GET", "PUT", "POST", "DELETE")); // 허용할 메서드
//        configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization")); // 허용할 헤더
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();  //UrlBasedCorsConfigurationSource를 통해 특정 URL 패턴에 규칙을 등록
//        source.registerCorsConfiguration("/**", configuration);
//
//        return source;
//
//    }
//


}
