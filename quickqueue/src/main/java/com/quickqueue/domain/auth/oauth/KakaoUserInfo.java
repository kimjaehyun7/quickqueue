package com.quickqueue.domain.auth.oauth;

import lombok.Getter;

import java.util.Map;

@Getter
public class KakaoUserInfo {

    private final String providerId;
    private final String email;
    private final String nickname;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.providerId = String.valueOf(attributes.get("id"));

        Map<String, Object> kakaoAccount =
                (Map<String, Object>) attributes.get("kakao_account");

        String tempEmail = (String) kakaoAccount.get("email");
        this.email = (tempEmail != null) ? tempEmail : providerId + "@kakao.user";

        Map<String, Object> profile =
                (Map<String, Object>) kakaoAccount.get("profile");

        String tempNickname = (String) profile.get("nickname");
        this.nickname = (tempNickname != null) ? tempNickname : "unknown" + providerId;
    }
}
