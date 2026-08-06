package com.ticketbooking.event.dto;

public record CreateVenueRequest(
    String name,
    String city,
    String address,
    Integer capacity
) {}
