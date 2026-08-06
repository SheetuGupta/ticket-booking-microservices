package com.ticketbooking.event.service;

import com.ticketbooking.event.dto.*;
import com.ticketbooking.event.entity.*;
import com.ticketbooking.event.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    public EventService(VenueRepository venueRepository, EventRepository eventRepository, SeatRepository seatRepository) {
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public Venue createVenue(CreateVenueRequest request) {
        Venue venue = new Venue();
        venue.setName(request.name());
        venue.setCity(request.city());
        venue.setAddress(request.address());
        venue.setCapacity(request.capacity());
        return venueRepository.save(venue);
    }

    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue not found"));

        Event event = new Event();
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setEventDateTime(request.eventDateTime());
        event.setVenue(venue);
        event.setBasePrice(request.basePrice());

        Event savedEvent = eventRepository.save(event);
        return mapToEventResponse(savedEvent);
    }

    @Transactional
    public List<SeatResponse> generateSeats(UUID eventId, GenerateSeatsRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Delete existing seats for this event if any, to avoid duplicate generation issues
        List<Seat> existingSeats = seatRepository.findByEventId(eventId);
        if (!existingSeats.isEmpty()) {
            seatRepository.deleteAll(existingSeats);
        }

        List<Seat> seatsToSave = new ArrayList<>();
        int totalRows = request.totalRows();
        int seatsPerRow = request.seatsPerRow();
        int vipRows = request.vipRows();

        for (int row = 1; row <= totalRows; row++) {
            // Convert row number to letter (1 -> A, 2 -> B, etc.)
            String rowLetter = getRowLetter(row);

            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                Seat seat = new Seat();
                seat.setEventId(eventId);
                seat.setSeatNumber(rowLetter + seatNum);
                seat.setStatus(SeatStatus.AVAILABLE);

                if (row <= vipRows) {
                    seat.setCategory(SeatCategory.VIP);
                    // VIP pricing: 1.5x of base price
                    seat.setPrice(event.getBasePrice().multiply(BigDecimal.valueOf(1.5)));
                } else {
                    seat.setCategory(SeatCategory.REGULAR);
                    seat.setPrice(event.getBasePrice());
                }

                seatsToSave.add(seat);
            }
        }

        List<Seat> savedSeats = seatRepository.saveAll(seatsToSave);
        return savedSeats.stream()
                .map(this::mapToSeatResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getAvailableSeats(UUID eventId) {
        return seatRepository.findByEventIdAndStatus(eventId, SeatStatus.AVAILABLE).stream()
                .map(this::mapToSeatResponse)
                .collect(Collectors.toList());
    }

    private String getRowLetter(int rowNum) {
        StringBuilder sb = new StringBuilder();
        while (rowNum > 0) {
            rowNum--;
            sb.insert(0, (char) ('A' + (rowNum % 26)));
            rowNum /= 26;
        }
        return sb.toString();
    }

    private EventResponse mapToEventResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getEventDateTime(),
                event.getVenue().getId(),
                event.getVenue().getName(),
                event.getBasePrice()
        );
    }

    private SeatResponse mapToSeatResponse(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getEventId(),
                seat.getSeatNumber(),
                seat.getCategory().name(),
                seat.getStatus().name(),
                seat.getPrice()
        );
    }
}
