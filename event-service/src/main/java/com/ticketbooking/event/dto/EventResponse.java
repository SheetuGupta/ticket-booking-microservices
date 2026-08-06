package com.ticketbooking.event.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventResponse(
    UUID id,
    String title,
    String description,
    LocalDateTime eventDateTime,
    UUID venueId,
    String venueName,
    BigDecimal basePrice
) {}
