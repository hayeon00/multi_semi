package com.multi.travel.common.jwt;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.multi.travel.common.exception.ApiExceptionDto;
import com.multi.travel.common.exception.TokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;

    private static final String[] EXACT_PATHS = {
            "/health-check"
    };

    private static final String[] WILDCARD_PATHS = {
          //  "/spots/view/**",
          //  "/courses/view/**",
         //   "/accommodations/view/**",
          //  "/reviews/target",
        //    "/members/view/**",
         //   "/admin/view/**",
            "/auth/**",
            "/public/**",
            "/swagger-ui/**",
         //   "/member/view/**"


    };


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("[JwtFilter] doFilterInternal START ===================================");
        String requestURI = request.getRequestURI();

        try {
            // 0. 로그인/회원가입/정적 리소스는 필터 통과
            if (isExcludedPath(requestURI)) {
                log.info("[JwtFilter] {} → JWT 필터 예외 경로, 필터 통과", requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            // 정확히 일치하는 경로는 필터를 건너뜀
            for (String exactPath : EXACT_PATHS) {
                if (requestURI.equals(exactPath)) {
                    filterChain.doFilter(request, response);
                    log.info("[JwtFilter] 요청 URI가 제외 경로에 해당하여 필터를 건너뜁니다.");

                    return;
                }
            }

            // 와일드카드 경로 매칭
            for (String wildcardPath : WILDCARD_PATHS) {
                if (requestURI.matches(wildcardPath.replace("**", ".*"))) {
                    log.info("[JwtFilter] 요청 URI가 제외 경로에 해당하여 필터를 건너뜁니다.");

                    filterChain.doFilter(request, response);
                    return;
                }
            }

            String jwt = resolveToken(request);
            log.info("[JwtFilter] jwt : {}", jwt);
            if (StringUtils.hasText(jwt)) {
                log.info("[JwtFilter] JWT 토큰이 존재합니다.");

                if (tokenProvider.validateToken(jwt)) {

                    log.info("[JwtFilter] JWT 토큰이 유효합니다.");

                    Authentication authentication = tokenProvider.getAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.info("[JwtFilter] SecurityContext에 Authentication 객체 설정 완료: {}", authentication);
                    log.info("[JwtFilter] SecurityContext에 Authentication 객체 설정 완료  authentication.getAuthorities(): {}", authentication.getAuthorities());

                    log.info("[JwtFilter] SecurityContextHolder 객체 확인: {}", SecurityContextHolder.getContext().getAuthentication());

                } else {
                    log.warn("[JwtFilter] JWT 토큰이 유효하지 않습니다.");
                }
            } else {
                log.info("[JwtFilter] JWT 토큰이 존재하지 않습니다.");
            }

            log.info("✅ [JwtFilter] Before filterChain.doFilter() → {}", SecurityContextHolder.getContext().getAuthentication());

            // 4. 필터 체인 계속 진행
            filterChain.doFilter(request, response);
            log.info("✅ [JwtFilter] After filterChain.doFilter() → {}", SecurityContextHolder.getContext().getAuthentication());
            log.info("[JwtFilter] 필터 체인 완료 후 응답 처리");

        } catch (TokenException e) {
            log.error("[JwtFilter] 필터 처리 중 예외 발생: {}", e.getMessage(), e);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            ApiExceptionDto errorResponse = new ApiExceptionDto(HttpStatus.UNAUTHORIZED, e.getMessage());

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(convertObjectToJson(errorResponse));
            response.getWriter().flush();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }

        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    log.info("[JwtFilter] 쿠키에서 access_token 찾음");
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public String convertObjectToJson(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    /**
     * 필터 예외 경로 (로그인, 회원가입, 정적자원 등)
     */
    private boolean isExcludedPath(String uri) {
        return uri.startsWith("/login") ||
                uri.startsWith("/signup") ||
                uri.startsWith("/css") ||
                uri.startsWith("/images") ||
                uri.startsWith("/js") ||
                uri.equals("/") ||
                uri.startsWith("/favicon");
    }

}