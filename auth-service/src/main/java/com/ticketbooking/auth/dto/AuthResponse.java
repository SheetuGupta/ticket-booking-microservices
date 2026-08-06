package com.ticketbooking.auth.dto;

public record AuthResponse(String token, String email, String role) {}
