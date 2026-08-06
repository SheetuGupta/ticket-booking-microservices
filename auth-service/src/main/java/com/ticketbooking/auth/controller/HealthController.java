package com.ticketbooking.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping({"/health", "/api/v1/auth/health"})
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("service", "auth-service");
        response.put("status", "UP");
        return response;
    }
}
