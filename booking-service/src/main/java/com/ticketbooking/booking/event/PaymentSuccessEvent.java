package com.ticketbooking.booking.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentSuccessEvent(
    UUID bookingId,
    UUID userId,
    UUID eventId,
    UUID seatId,
    BigDecimal amount,
    String transactionId
) {}
