package com.quickqueue.global.auth.jwt;

import com.quickqueue.domain.member.entity.Member;
import com.quickqueue.domain.member.repository.MemberRepository;
import com.quickqueue.global.auth.user.CustomUserDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Authorization 헤더 가져오기
        String authorization = request.getHeader("Authorization");

        // JWT 가 없으면 다음 필터로
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bearer 제거
        String token = authorization.substring(7);

        try {
            // JWT 검증 및 Claims 추출
            Claims claims = jwtTokenProvider.parseClaims(token);

            // JWT 의 subject -> memberId
            Long memberId = Long.valueOf(claims.getSubject());

            // member 조회
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));// TODO

            // 권한 생성
            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority("ROLE_" + member.getRole().name());

            // UserDetails 생성
            CustomUserDetails userDetails = new CustomUserDetails(memberId);

            // Spring Security 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, List.of(authority));

            // 현재 요청에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // 잘못된 JWT 는 인증 정보를 설정하지 않음
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
