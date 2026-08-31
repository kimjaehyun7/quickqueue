package com.quickqueue.domain.reservation.service;

import com.quickqueue.global.config.SolapiConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsNotificationService implements NotificationService{

    private final SolapiConfig solapiConfig;
    private DefaultMessageService messageService;

    @PostConstruct
    void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(
                solapiConfig.getApiKey(),
                solapiConfig.getApiSecret(),
                "https://api.solapi.com"
        );
    }

    @Override
    public void sendReservationCompleted(String phoneNumber,
                                         int waitingNumber,
                                         long waitingAhead,
                                         String reservationUrl) {
        String text = """
                [QuickQueue]
                예약이 완료되었습니다.
                                
                대기번호: %d번
                내 앞 대기자: %d명
                                
                차례가 되면 다시 문자로 알려드립니다.
                
                실시간 예약 현황:
                %s
                """.formatted(
                waitingNumber,
                waitingAhead,
                reservationUrl
        );

        sendSms(phoneNumber, text);
    }

    @Override
    public void sendCalled(String phoneNumber) {
        String text = """
                [QuickQueue]
                입장하실 차례입니다.
                매장으로 와주세요.
                """;

        sendSms(phoneNumber, text);
    }

    private void sendSms(String phoneNumber, String text) {
        Message message = new Message();
        message.setFrom(solapiConfig.getSender());
        message.setTo(phoneNumber);
        message.setText(text);

        try {
            messageService.send(message);
        } catch (NurigoMessageNotReceivedException e) {
            log.error("메시지 전송 중 오류 발생 failedMessageList={}", e.getFailedMessageList());
            log.error("에러 메시지 message={}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("메시지 전송 중 오류 발생"); // TODO
        } catch (Exception e) {
            log.error("메시지 전송 중 오류 발생 phoneNumber={}", phoneNumber, e);
            throw new RuntimeException("메시지 전송 중 오류 발생"); // TODO
        }

    }
}
