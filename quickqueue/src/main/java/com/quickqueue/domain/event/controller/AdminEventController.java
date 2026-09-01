package com.quickqueue.domain.event.controller;

import com.quickqueue.domain.event.dto.EventRequest;
import com.quickqueue.domain.event.dto.EventResponse;
import com.quickqueue.domain.event.service.EventService;
import com.quickqueue.global.auth.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{publicId}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable String publicId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();

        return ResponseEntity.ok(eventService.getEvent(memberId, publicId));
    }

    @PostMapping("/{publicId}/close")
    public ResponseEntity<Void> closeEvent(@PathVariable String publicId,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();

        eventService.closeEvent(publicId, memberId);

        return ResponseEntity.noContent().build();
    }
}
