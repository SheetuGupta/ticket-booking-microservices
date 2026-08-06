package com.ticketbooking.booking.repository;

import com.ticketbooking.booking.entity.Booking;
import com.ticketbooking.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime dateTime);
}
