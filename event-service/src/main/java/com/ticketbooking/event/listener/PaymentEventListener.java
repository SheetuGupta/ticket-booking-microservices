package com.ticketbooking.event.listener;

import com.ticketbooking.event.entity.SeatStatus;
import com.ticketbooking.event.event.PaymentSuccessEvent;
import com.ticketbooking.event.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final EventService eventService;

    public PaymentEventListener(EventService eventService) {
        this.eventService = eventService;
    }

    @KafkaListener(
            topics = "payment-events-topic",
            groupId = "event-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("Received PaymentSuccessEvent for seatId: {}", event.seatId());
        try {
            eventService.updateSeatStatus(event.seatId(), SeatStatus.BOOKED);
            log.info("Seat status updated to BOOKED for seatId: {}", event.seatId());
        } catch (Exception e) {
            log.error("Failed to update seat status to BOOKED for seatId: {}", event.seatId(), e);
        }
    }
}
