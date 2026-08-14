package com.quickqueue.domain.auth.controller;

import com.quickqueue.domain.auth.dto.AccessTokenResponse;
import com.quickqueue.domain.auth.service.TokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final TokenService tokenService;

    @PostMapping("/reissue")
    public ResponseEntity<AccessTokenResponse> reissue(@CookieValue("refreshToken") String refreshToken) {

        String accessToken = tokenService.reissueAccessToken(refreshToken);

        AccessTokenResponse response = new AccessTokenResponse(accessToken);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue("refreshToken") String refreshToken,
                                       HttpServletResponse response) {
        tokenService.logout(refreshToken);

        // refresh Token Cookie 삭제
        ResponseCookie cookie =
                ResponseCookie.from("refreshToken", "")
                        .httpOnly(true)
                        .secure(false)
                        .path("/")
                        .maxAge(0)
                        .sameSite("Lax")
                        .build();
        response.addHeader(
                HttpHeaders.SET_COOKIE, cookie.toString()
        );

        return ResponseEntity.noContent().build();
    }
}
