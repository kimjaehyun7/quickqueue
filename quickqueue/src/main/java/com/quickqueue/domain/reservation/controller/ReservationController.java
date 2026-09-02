package com.quickqueue.domain.reservation.controller;

import com.quickqueue.domain.reservation.dto.ReservationRequest;
import com.quickqueue.domain.reservation.dto.ReservationResponse;
import com.quickqueue.domain.reservation.dto.ReservationStatusResponse;
import com.quickqueue.domain.reservation.service.ReservationService;
import com.quickqueue.domain.reservation.service.SseEmitters;
import com.quickqueue.global.auth.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final SseEmitters sseEmitters;

    @PostMapping("/{publicId}")
    public ResponseEntity<ReservationResponse> createReservation(@PathVariable String publicId,
                                            @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(reservationService.createReservation(publicId, request));
    }

    @GetMapping("/{reservationToken}")
    public ResponseEntity<ReservationStatusResponse> getReservation(@PathVariable String reservationToken) {
        return ResponseEntity.ok(reservationService.getReservation(reservationToken));
    }

    @GetMapping(value = "/{reservationToken}/sse/connect",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sseConnect(@PathVariable String reservationToken) {
        SseEmitter emitter = sseEmitters.subscribe(reservationToken);

        ReservationStatusResponse response = reservationService.getReservation(reservationToken);

        switch (response.status()) {
            case CALLED -> sseEmitters.send(reservationToken, "called", response);
            case WAITING -> sseEmitters.send(reservationToken, "waiting", response);
            case CANCELED -> sseEmitters.send(reservationToken, "canceled", response);
            case COMPLETED -> sseEmitters.send(reservationToken, "completed", response);
        }

        return emitter;
    }

    @GetMapping(value = "/{publicId}/admin/sse/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter adminSseConnect(@PathVariable String publicId,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();

        SseEmitter emitter = sseEmitters.subscribe(publicId);

        List<ReservationResponse> reservations = reservationService.getReservations(memberId, publicId);

        sseEmitters.send(publicId, "connected", reservations);

        return emitter;
    }
}
