package com.quickqueue.domain.reservation.controller;

import com.quickqueue.domain.reservation.dto.ReservationRequest;
import com.quickqueue.domain.reservation.dto.ReservationResponse;
import com.quickqueue.domain.reservation.dto.ReservationStatusResponse;
import com.quickqueue.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/events/{publicId}/reservations")
    public ReservationResponse createReservation(@PathVariable String publicId,
                                                 @RequestBody ReservationRequest request) {
        return reservationService.createReservation(publicId, request);
    }

    @GetMapping("/reservations/{reservationToken}")
    public ReservationStatusResponse getReservation(@PathVariable String reservationToken) {
        return reservationService.getReservation(reservationToken);
    }
}
