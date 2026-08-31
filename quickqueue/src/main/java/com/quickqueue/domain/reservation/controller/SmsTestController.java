package com.quickqueue.domain.reservation.controller;

import com.quickqueue.domain.reservation.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class SmsTestController {

    private final NotificationService notificationService;

    @PostMapping("/sms")
    public ResponseEntity<Void> sendSms(@RequestParam String phoneNumber) {
        notificationService.sendCalled(phoneNumber);

        return ResponseEntity.noContent().build();
    }
}
