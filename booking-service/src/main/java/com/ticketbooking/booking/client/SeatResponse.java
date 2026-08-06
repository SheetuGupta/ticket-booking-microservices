package com.ticketbooking.booking.client;

import java.math.BigDecimal;
import java.util.UUID;

public record SeatResponse(
    UUID id,
    UUID eventId,
    String seatNumber,
    String category,
    String status,
    BigDecimal price
) {}
