package com.quickqueue.global.config;

import com.quickqueue.domain.auth.service.CustomOAuth2UserService;
import com.quickqueue.global.auth.jwt.JwtAuthenticationFilter;
import com.quickqueue.global.auth.oauth.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        return http
                // cors
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // csrf
                .csrf(csrf -> csrf.disable())

                // 세션을 사용하지 않고 JWT 로 인증
                // .sessionManagement(session -> session
                //         .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // h2 허용
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login/**",
                                "/oauth2/**",
                                "/api/auth/reissue",
                                "/api/auth/logout",
                                "/api/reservations/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // 인증되지 않은 요청을 로그인 페이지로 보내지 않고 401 반환
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                ((request, response, authException) -> {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.setContentType("application/json;charset=UTF-8");
                                    response.getWriter().write("{\"message\":\"인증이 필요합니다.\"}");
                                })
                        )
                )

                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler))
                // jwt 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 프론트 도메인 주소
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));

        // http 메서드
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 헤더
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-type"));

        // 자격 증명(쿠키, 리프레시 토큰, 인증 헤더 등)
        configuration.setAllowCredentials(true);

        // 브라우저가 응답 헤더에 접근할 수 있도록
        configuration.setExposedHeaders(List.of("Authorization"));

        // 모든 경로에 위 정책을 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
