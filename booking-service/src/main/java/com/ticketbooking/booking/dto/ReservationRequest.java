package com.ticketbooking.booking.dto;

import java.util.UUID;

public record ReservationRequest(
    UUID eventId,
    UUID seatId,
    UUID userId
) {}
