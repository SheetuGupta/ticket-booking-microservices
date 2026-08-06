package com.ticketbooking.event.dto;

public record GenerateSeatsRequest(
    Integer totalRows,
    Integer seatsPerRow,
    Integer vipRows
) {}
