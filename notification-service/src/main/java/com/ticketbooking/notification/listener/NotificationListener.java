package com.ticketbooking.notification.listener;

import com.ticketbooking.notification.event.BookingConfirmedEvent;
import com.ticketbooking.notification.event.PaymentSuccessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    @KafkaListener(
            topics = "payment-events-topic",
            groupId = "notification-group",
            containerFactory = "paymentKafkaListenerContainerFactory"
    )
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("-------------------- PAYMENT RECEIPT --------------------");
        log.info("Processing notification receipt for Payment SUCCESS");
        log.info("Transaction ID : {}", event.transactionId());
        log.info("Booking ID     : {}", event.bookingId());
        log.info("User ID        : {}", event.userId());
        log.info("Seat ID        : {}", event.seatId());
        log.info("Amount Paid    : ${}", event.amount());
        log.info("---------------------------------------------------------");
    }

    @KafkaListener(
            topics = "booking-notifications-topic",
            groupId = "notification-group",
            containerFactory = "bookingKafkaListenerContainerFactory"
    )
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        log.info("==================== BOOKING CONFIRMATION EMAIL ====================");
        log.info("To User ID     : {}", event.userId());
        log.info("Subject        : Booking Confirmation for Booking Ref: {}", event.bookingId());
        log.info("Body           :\n" +
                "                 Dear Customer,\n" +
                "                 Your booking is CONFIRMED!\n\n" +
                "                 --- Booking Summary ---\n" +
                "                 Booking Reference : " + event.bookingId() + "\n" +
                "                 Event Reference   : " + event.eventId() + "\n" +
                "                 Seat Reference    : " + event.seatId() + "\n" +
                "                 Transaction ID    : " + event.transactionId() + "\n" +
                "                 Total Amount Paid : $" + event.amount() + "\n" +
                "                 Confirmed At      : " + event.confirmedAt() + "\n\n" +
                "                 Thank you for choosing us!");
        log.info("====================================================================");
    }
}
