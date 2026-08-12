package com.quickqueue.domain.member.repository;

import com.quickqueue.domain.member.entity.Member;
import com.quickqueue.domain.member.entity.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByProviderAndProviderId(OAuthProvider provider, String providerId);
}
