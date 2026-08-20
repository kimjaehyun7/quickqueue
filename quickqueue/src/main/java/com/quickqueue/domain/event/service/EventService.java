package com.quickqueue.domain.event.service;

import com.quickqueue.domain.event.dto.EventRequest;
import com.quickqueue.domain.event.dto.EventResponse;
import com.quickqueue.domain.event.entity.Event;
import com.quickqueue.domain.event.repository.EventRepository;
import com.quickqueue.domain.member.entity.Member;
import com.quickqueue.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
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

    private String makePublicId() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);
    }
}
