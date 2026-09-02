package com.quickqueue.domain.reservation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseEmitters {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 구독
    public SseEmitter subscribe(String token) {
        SseEmitter sseEmitter = new SseEmitter(60L * 60 * 1000); // 1시간 타임 아웃
        emitters.put(token, sseEmitter);
        log.info("sse 연결 성공 token={}", token);

        // 콜백 설정
        sseEmitter.onCompletion(()->{
            log.info("sse 연결 정상 종료 token={}", token);
            emitters.remove(token);
        });
        sseEmitter.onTimeout(()->{
            log.info("sse 연결 타임아웃 token={}", token);
            emitters.remove(token);
        });
        sseEmitter.onError((e)->{
            log.error("sse 연결 에러 token={}", token, e);
            emitters.remove(token);
        });


        return sseEmitter;
    }

    // 전송

    /**
     *
     * @param token 토큰
     * @param name 프론트에서 수신할 데이터의 이름
     * @param data 전송할 데이터
     */
    public void send(String token, String name, Object data) {
        SseEmitter sseEmitter = emitters.get(token);
        if (sseEmitter != null) {
            try {
                sseEmitter.send(SseEmitter.event()
                        .name(name)
                        .data(data)
                );
            } catch (IOException e) {
                log.error("sse 전송 중 에러 token={}", token);
                emitters.remove(token);
            }
        } else {
            log.debug("해당 토큰에 대한 sse 연결이 존재하지 않습니다.");
        }
    }

    // sse 연결 완료
    public void complete(String token) {
        SseEmitter sseEmitter = emitters.get(token);
        if (sseEmitter != null) {
            sseEmitter.complete();
            emitters.remove(token);
        }
    }
}
