package com.ticketbooking.booking.service;

import com.ticketbooking.booking.client.EventClient;
import com.ticketbooking.booking.client.SeatResponse;
import com.ticketbooking.booking.dto.BookingResponse;
import com.ticketbooking.booking.dto.ReservationRequest;
import com.ticketbooking.booking.entity.Booking;
import com.ticketbooking.booking.entity.BookingStatus;
import com.ticketbooking.booking.repository.BookingRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventClient eventClient;
    private final RedissonClient redissonClient;

    public BookingService(BookingRepository bookingRepository, EventClient eventClient, RedissonClient redissonClient) {
        this.bookingRepository = bookingRepository;
        this.eventClient = eventClient;
        this.redissonClient = redissonClient;
    }

    @Transactional
    public BookingResponse reserveSeat(ReservationRequest request) {
        String lockKey = "lock:seat:" + request.seatId();
        RLock lock = redissonClient.getLock(lockKey);
        
        boolean isLocked = false;
        try {
            // Try to acquire lock within 3 seconds, release lock after 10 seconds if not released manually
            isLocked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new IllegalStateException("Could not acquire seat lock. Please try again.");
            }

            // Call event-service via Feign to check seat status
            SeatResponse seat = eventClient.getSeat(request.seatId());
            if (!"AVAILABLE".equals(seat.status())) {
                throw new IllegalStateException("Seat is not available for booking");
            }

            // Update seat status in event-service to RESERVED
            eventClient.updateSeatStatus(request.seatId(), "RESERVED");

            // Save Booking record in PENDING status, expiring in 10 minutes
            Booking booking = new Booking();
            booking.setUserId(request.userId());
            booking.setEventId(request.eventId());
            booking.setSeatId(request.seatId());
            booking.setStatus(BookingStatus.PENDING);
            booking.setTotalAmount(seat.price());
            booking.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            
            Booking savedBooking = bookingRepository.save(booking);
            return mapToBookingResponse(savedBooking);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Lock acquisition interrupted", e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        return mapToBookingResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only PENDING or CONFIRMED bookings can be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updatedBooking = bookingRepository.save(booking);

        // Mark seat back to AVAILABLE in event-service
        eventClient.updateSeatStatus(booking.getSeatId(), "AVAILABLE");

        return mapToBookingResponse(updatedBooking);
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getEventId(),
                booking.getSeatId(),
                booking.getStatus().name(),
                booking.getTotalAmount(),
                booking.getCreatedAt(),
                booking.getExpiresAt()
        );
    }
}
