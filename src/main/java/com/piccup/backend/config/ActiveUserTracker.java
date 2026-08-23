package com.piccup.backend.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ActiveUserTracker {

    // userId -> 마지막 활동 시간(API 요청 시간)을 저장
    private final ConcurrentHashMap<Long, Instant> activeUsers = new ConcurrentHashMap<>();

    public ActiveUserTracker(MeterRegistry meterRegistry) {
        // 프로메테우스에 'custom_realtime_users' 라는 이름으로 Map의 사이즈(현재 유저 수) 등록
        Gauge.builder("custom_realtime_users", activeUsers, Map::size)
                .description("최근 5분 이내 활동한 실시간 유저 수")
                .register(meterRegistry);
    }

    // 인터셉터에서 호출할 메서드: 유저가 API를 호출할 때마다 활동 시간을 현재로 갱신
    public void markUserActive(Long userId) {
        activeUsers.put(userId, Instant.now());
    }

    // 1분마다 백그라운드에서 실행하며 5분이 지난 유저(미활동자)는 리스트에서 삭제
    @Scheduled(fixedRate = 60000)
    public void cleanupIdleUsers() {
        Instant threshold = Instant.now().minus(Duration.ofMinutes(5));
        activeUsers.entrySet().removeIf(entry -> entry.getValue().isBefore(threshold));
        log.debug("정리 완료. 현재 접속자 수: {}", activeUsers.size());
    }

    // 수동 로그아웃 유저 반영용
    public void removeUser(Long userId) {
        activeUsers.remove(userId);
    }
}