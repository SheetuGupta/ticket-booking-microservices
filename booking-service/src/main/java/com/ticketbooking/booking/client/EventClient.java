package com.ticketbooking.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "EVENT-SERVICE", path = "/api/v1/events")
public interface EventClient {

    @GetMapping("/seats/{seatId}")
    SeatResponse getSeat(@PathVariable("seatId") UUID seatId);

    @PutMapping("/seats/{seatId}/status")
    SeatResponse updateSeatStatus(
            @PathVariable("seatId") UUID seatId,
            @RequestParam("status") String status
    );
}
