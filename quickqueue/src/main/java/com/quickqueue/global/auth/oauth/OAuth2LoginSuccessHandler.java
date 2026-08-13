package com.quickqueue.global.auth.oauth;

import com.quickqueue.domain.auth.dto.TokenResponse;
import com.quickqueue.domain.auth.service.TokenService;
import com.quickqueue.domain.member.entity.Member;
import com.quickqueue.domain.member.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final TokenService tokenService;

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
        TokenResponse tokenResponse = tokenService.issueToken(member);

        // 테스트용 응답 출력
        // TODO : 추후 access token 은 헤더, refresh token 은 쿠키로 전달하는 방식으로 수정
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(
                "로그인 성공\n" +
                        "Access Token: " + tokenResponse.accessToken() + "\n" +
                        "Refresh Token: " + tokenResponse.refreshToken()
        );
    }
}
