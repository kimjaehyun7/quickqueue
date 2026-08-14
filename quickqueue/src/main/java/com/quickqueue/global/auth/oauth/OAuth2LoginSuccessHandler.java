package com.quickqueue.global.auth.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickqueue.domain.auth.dto.TokenPair;
import com.quickqueue.domain.auth.service.TokenService;
import com.quickqueue.domain.member.entity.Member;
import com.quickqueue.domain.member.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        // OAuth2 로그인 사용자 정보
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // CustomOAuth2UserService 에서 넣어준 memberId
        long memberId = ((Number) oAuth2User.getAttributes()
                .get("memberId")).longValue();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다.")); // TODO : 추후 에러코드로 변경

        // 토큰 발급
        TokenPair tokenPair = tokenService.issueToken(member);

        // refresh token -> HttpOnly Cookie 에 저장
        ResponseCookie refreshTokenCookie =
                ResponseCookie.from(
                                "refreshToken",
                                tokenPair.refreshToken()
                        )
                        .httpOnly(true)
                        .secure(false)
                        .path("/")
                        .maxAge(Duration.ofDays(14))
                        .sameSite("Lax") // 웹 사이트 간 쿠키 전송을 제한하여 보안 강화
                        .build();
        response.addHeader(HttpHeaders.SET_COOKIE,
                refreshTokenCookie.toString());

        // access Token 만 전달
        response.setContentType("application/json;charset=UTF-8");

        Map<String, String> responseBody = Map.of("accessToken", tokenPair.accessToken());
        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
    }
}
