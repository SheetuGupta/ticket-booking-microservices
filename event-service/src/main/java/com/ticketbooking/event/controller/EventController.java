package com.ticketbooking.event.controller;

import com.ticketbooking.event.dto.*;
import com.ticketbooking.event.entity.Venue;
import com.ticketbooking.event.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/venues")
    public ResponseEntity<Venue> createVenue(@RequestBody CreateVenueRequest request) {
        return ResponseEntity.ok(eventService.createVenue(request));
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody CreateEventRequest request) {
        return ResponseEntity.ok(eventService.createEvent(request));
    }

    @PostMapping("/{eventId}/seats/generate")
    public ResponseEntity<List<SeatResponse>> generateSeats(
            @PathVariable("eventId") UUID eventId,
            @RequestBody GenerateSeatsRequest request
    ) {
        return ResponseEntity.ok(eventService.generateSeats(eventId, request));
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{eventId}/seats")
    public ResponseEntity<List<SeatResponse>> getAvailableSeats(@PathVariable("eventId") UUID eventId) {
        return ResponseEntity.ok(eventService.getAvailableSeats(eventId));
    }

    @GetMapping("/seats/{seatId}")
    public ResponseEntity<SeatResponse> getSeat(@PathVariable("seatId") UUID seatId) {
        return ResponseEntity.ok(eventService.getSeat(seatId));
    }

    @PutMapping("/seats/{seatId}/status")
    public ResponseEntity<SeatResponse> updateSeatStatus(
            @PathVariable("seatId") UUID seatId,
            @RequestParam("status") com.ticketbooking.event.entity.SeatStatus status
    ) {
        return ResponseEntity.ok(eventService.updateSeatStatus(seatId, status));
    }
}
