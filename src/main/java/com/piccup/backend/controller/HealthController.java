package com.piccup.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController {

    // DB에 쿼리를 날리기 위함
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        // 더미 쿼리를 실행
        jdbcTemplate.queryForObject("SELECT 1", Integer.class);

        return ResponseEntity.ok("Backend and DB are alive!");
    }
}