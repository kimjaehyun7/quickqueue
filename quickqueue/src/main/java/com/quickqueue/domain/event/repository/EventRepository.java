package com.quickqueue.domain.event.repository;

import com.quickqueue.domain.event.entity.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    // 비관적 락
    // 동시성 문제 해결
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select e
            from Event e
            where e.publicId = :publicId
            """)
    Optional<Event> findByPublicIdUseLock(@Param("publicId") String publicId);

    Optional<Event> findByPublicId(@Param("publicId") String publicId);

    List<Event> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

}
