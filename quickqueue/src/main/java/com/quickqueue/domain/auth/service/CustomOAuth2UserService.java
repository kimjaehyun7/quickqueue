package com.quickqueue.domain.auth.service;

import com.quickqueue.domain.auth.oauth.KakaoUserInfo;
import com.quickqueue.domain.member.entity.Member;
import com.quickqueue.domain.member.entity.OAuthProvider;
import com.quickqueue.domain.member.entity.Role;
import com.quickqueue.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        KakaoUserInfo kakaoUserInfo =
                new KakaoUserInfo(oAuth2User.getAttributes());

        memberRepository.findByProviderAndProviderId(
                        OAuthProvider.KAKAO,
                        kakaoUserInfo.getProviderId())
                .orElseGet(() -> createMember(kakaoUserInfo));

        return oAuth2User;
    }

    private Member createMember(KakaoUserInfo kakaoUserInfo) {
        Member member = Member.create(kakaoUserInfo.getEmail(),
                kakaoUserInfo.getNickname(),
                OAuthProvider.KAKAO,
                kakaoUserInfo.getProviderId(),
                Role.ADMIN
        );
        return memberRepository.save(member);
    }
}
