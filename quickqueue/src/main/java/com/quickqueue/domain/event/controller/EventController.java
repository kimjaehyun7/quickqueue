package com.quickqueue.domain.event.controller;

import com.quickqueue.domain.event.dto.EventRequest;
import com.quickqueue.domain.event.dto.EventResponse;
import com.quickqueue.domain.event.service.EventService;
import com.quickqueue.global.auth.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/events")
public class EventController {

    private final EventService eventService;

    @PostMapping
    public EventResponse createEvent(@RequestBody EventRequest request,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();

        return eventService.createEvent(memberId, request);
    }
}
