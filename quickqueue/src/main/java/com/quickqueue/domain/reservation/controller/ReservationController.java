package com.quickqueue.domain.reservation.controller;

import com.quickqueue.domain.reservation.dto.ReservationRequest;
import com.quickqueue.domain.reservation.dto.ReservationResponse;
import com.quickqueue.domain.reservation.dto.ReservationStatusResponse;
import com.quickqueue.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/{publicId}")
    public ResponseEntity<ReservationResponse> createReservation(@PathVariable String publicId,
                                            @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(reservationService.createReservation(publicId, request));
    }

    @GetMapping("/{reservationToken}")
    public ResponseEntity<ReservationStatusResponse> getReservation(@PathVariable String reservationToken) {
        return ResponseEntity.ok(reservationService.getReservation(reservationToken));
    }
}
