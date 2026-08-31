package com.quickqueue.domain.reservation.service;

public interface NotificationService {

    // 예약 완료
    void sendReservationCompleted(String phoneNumber,
                                  int waitingNumber,
                                  long waitingAhead,
                                  String reservationUrl
    );

    // 호출
    void sendCalled(String phoneNumber);
}
