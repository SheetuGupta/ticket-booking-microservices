package com.ticketbooking.event.repository;

import com.ticketbooking.event.entity.Seat;
import com.ticketbooking.event.entity.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {

    List<Seat> findByEventId(UUID eventId);

    List<Seat> findByEventIdAndStatus(UUID eventId, SeatStatus status);

    @Query(value = "SELECT * FROM seats WHERE event_id = :eventId AND status = 'AVAILABLE' FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<Seat> findAvailableSeatsWithLock(@Param("eventId") UUID eventId);
}
