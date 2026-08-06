package com.ticketbooking.booking.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping({"/health", "/api/v1/bookings/health"})
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("service", "booking-service");
        response.put("status", "UP");
        return response;
    }
}
