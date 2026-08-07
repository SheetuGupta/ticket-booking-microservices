package com.ticketbooking.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "BOOKING-SERVICE", path = "/api/v1/bookings")
public interface BookingClient {

    @GetMapping("/{bookingId}")
    BookingResponse getBooking(@PathVariable("bookingId") UUID bookingId);
}
