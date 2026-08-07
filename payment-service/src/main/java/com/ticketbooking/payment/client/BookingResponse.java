package com.ticketbooking.payment.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponse(
    UUID id,
    UUID userId,
    UUID eventId,
    UUID seatId,
    String status,
    BigDecimal totalAmount,
    LocalDateTime createdAt,
    LocalDateTime expiresAt
) {}
