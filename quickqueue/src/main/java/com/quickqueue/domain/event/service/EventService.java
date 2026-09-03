package com.quickqueue.domain.event.service;

import com.quickqueue.domain.event.dto.EventRequest;
import com.quickqueue.domain.event.dto.EventResponse;
import com.quickqueue.domain.event.entity.Event;
import com.quickqueue.domain.event.repository.EventRepository;
import com.quickqueue.domain.member.entity.Member;
import com.quickqueue.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;

    public EventResponse createEvent(Long memberId, EventRequest request) {

        String publicId = makePublicId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(
                        // TODO
                );

        Event event = Event.create(member, request, publicId);

        eventRepository.save(event);

        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getPublicId(),
                event.getStatus()
        );
    }

    public EventResponse getEvent(Long memberId, String publicId) {

        Event event = eventRepository.findByPublicId(publicId)
                .orElseThrow(
                        // TODO
                );

        if (!event.isOwner(memberId)) {
            log.error("권한이 없습니다. eventMemberId={}, requestMemberId={}", event.getMember().getId(), memberId);
            throw new RuntimeException("해당 이벤트에 접근할 권한이 없습니다.");
            // TODO
        }

        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getPublicId(),
                event.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public EventResponse getPublicEvent(String publicId) {
        Event event = eventRepository.findByPublicId(publicId)
                .orElseThrow(
                        // TODO
                );

        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getPublicId(),
                event.getStatus()
        );
    }

    public List<EventResponse> getEvents(Long memberId) {
        return eventRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId).stream()
                .map(e -> new EventResponse(
                        e.getId(),
                        e.getName(),
                        e.getPublicId(),
                        e.getStatus())
                )
                .toList();
    }

    public void closeEvent(String publicId, Long memberId) {
        Event event = eventRepository.findByPublicId(publicId)
                .orElseThrow(
                        // TODO
                );

        if (!event.isOwner(memberId)) {
            log.error("권한이 없습니다. eventMemberId={}, requestMemberId={}", event.getMember().getId(), memberId);
            throw new RuntimeException("해당 이벤트에 접근할 권한이 없습니다.");
            // TODO
        }

        event.close();
    }

    private String makePublicId() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);
    }
}
