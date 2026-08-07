package com.ticketbooking.booking.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingConfirmedEvent(
    UUID bookingId,
    UUID userId,
    UUID eventId,
    UUID seatId,
    BigDecimal amount,
    String transactionId,
    LocalDateTime confirmedAt
) {}
