package com.piccup.backend;

import org.springframework.web.bind.annotation.*;

@RestController
public class HealthController {
    @GetMapping("/health")
    public String health() { return "ok"; }
}
