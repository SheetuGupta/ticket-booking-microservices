package com.ticketbooking.auth.dto;

public record RegisterRequest(String fullName, String email, String password, String role) {}
