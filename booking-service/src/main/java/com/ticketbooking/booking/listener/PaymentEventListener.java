package com.ticketbooking.booking.listener;

import com.ticketbooking.booking.entity.Booking;
import com.ticketbooking.booking.entity.BookingStatus;
import com.ticketbooking.booking.event.BookingConfirmedEvent;
import com.ticketbooking.booking.event.PaymentSuccessEvent;
import com.ticketbooking.booking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final BookingRepository bookingRepository;
    private final KafkaTemplate<String, BookingConfirmedEvent> kafkaTemplate;

    public PaymentEventListener(BookingRepository bookingRepository,
                                  KafkaTemplate<String, BookingConfirmedEvent> kafkaTemplate) {
        this.bookingRepository = bookingRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
            topics = "payment-events-topic",
            groupId = "booking-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("Received PaymentSuccessEvent for bookingId: {}", event.bookingId());

        Booking booking = bookingRepository.findById(event.bookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + event.bookingId()));

        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            log.info("Booking status updated to CONFIRMED for bookingId: {}", booking.getId());

            // Publish BookingConfirmedEvent to booking-notifications-topic
            BookingConfirmedEvent confirmedEvent = new BookingConfirmedEvent(
                    booking.getId(),
                    booking.getUserId(),
                    booking.getEventId(),
                    booking.getSeatId(),
                    booking.getTotalAmount(),
                    event.transactionId(),
                    LocalDateTime.now()
            );
            kafkaTemplate.send("booking-notifications-topic", booking.getId().toString(), confirmedEvent);
            log.info("Published BookingConfirmedEvent to booking-notifications-topic for bookingId: {}", booking.getId());
        } else {
            log.warn("Booking {} is not in PENDING status (current status: {}). Skipping update.", 
                    booking.getId(), booking.getStatus());
        }
    }
}
