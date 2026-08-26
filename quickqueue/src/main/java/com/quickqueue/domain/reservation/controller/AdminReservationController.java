package com.quickqueue.domain.reservation.controller;

import com.quickqueue.domain.reservation.dto.ReservationResponse;
import com.quickqueue.domain.reservation.service.ReservationService;
import com.quickqueue.global.auth.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/reservations/{publicId}")
public class AdminReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservations(@PathVariable String publicId,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();

        return ResponseEntity.ok(reservationService.getReservations(memberId, publicId));
    }

    @PostMapping("/{reservationToken}/call")
    public ResponseEntity<Void> callReservation(@PathVariable String publicId,
                                                @PathVariable String reservationToken,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();

        reservationService.callReservation(memberId, publicId, reservationToken);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{reservationToken}/complete")
    public ResponseEntity<Void> completeReservation(@PathVariable String publicId,
                                                @PathVariable String reservationToken,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();

        reservationService.completeReservation(memberId, publicId, reservationToken);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{reservationToken}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable String publicId,
                                                  @PathVariable String reservationToken,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();

        reservationService.cancelReservation(memberId, publicId, reservationToken);


        return ResponseEntity.noContent().build();
    }
}
