package com.ticketbooking.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
    UUID paymentId,
    UUID bookingId,
    UUID userId,
    BigDecimal amount,
    String paymentStatus,
    String transactionId,
    LocalDateTime createdAt
) {}
