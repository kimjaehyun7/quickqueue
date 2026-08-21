package com.quickqueue.domain.event.controller;

import com.quickqueue.domain.event.dto.EventRequest;
import com.quickqueue.domain.event.dto.EventResponse;
import com.quickqueue.domain.event.service.EventService;
import com.quickqueue.global.auth.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/events")
public class AdminEventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventRequest request,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();

        return ResponseEntity.ok(eventService.createEvent(memberId, request));
    }
}
