package com.ticketbooking.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentRequest(
    UUID bookingId,
    BigDecimal amount,
    String paymentMethod
) {}
