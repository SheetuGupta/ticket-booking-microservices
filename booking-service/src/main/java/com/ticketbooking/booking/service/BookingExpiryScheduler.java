package com.ticketbooking.booking.service;

import com.ticketbooking.booking.client.EventClient;
import com.ticketbooking.booking.entity.Booking;
import com.ticketbooking.booking.entity.BookingStatus;
import com.ticketbooking.booking.repository.BookingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingExpiryScheduler {

    private final BookingRepository bookingRepository;
    private final EventClient eventClient;

    public BookingExpiryScheduler(BookingRepository bookingRepository, EventClient eventClient) {
        this.bookingRepository = bookingRepository;
        this.eventClient = eventClient;
    }

    @Scheduled(fixedRate = 60000) // Every 1 minute
    @Transactional
    public void cleanupExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(
                BookingStatus.PENDING, now
        );

        for (Booking booking : expiredBookings) {
            try {
                booking.setStatus(BookingStatus.EXPIRED);
                bookingRepository.save(booking);
                
                // Release seat back to AVAILABLE in event-service
                eventClient.updateSeatStatus(booking.getSeatId(), "AVAILABLE");
            } catch (Exception e) {
                System.err.println("Failed to expire booking " + booking.getId() + ": " + e.getMessage());
            }
        }
    }
}
