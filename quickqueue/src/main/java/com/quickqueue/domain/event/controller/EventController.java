package com.quickqueue.domain.event.controller;

import com.quickqueue.domain.event.dto.EventResponse;
import com.quickqueue.domain.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @GetMapping("/{publicId}")
    public ResponseEntity<EventResponse> getPublicEvent(@PathVariable String publicId) {
        return ResponseEntity.ok(eventService.getPublicEvent(publicId));
    }
}