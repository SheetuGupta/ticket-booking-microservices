package com.ticketbooking.payment.service;

import com.ticketbooking.payment.client.BookingClient;
import com.ticketbooking.payment.client.BookingResponse;
import com.ticketbooking.payment.dto.PaymentResponse;
import com.ticketbooking.payment.dto.ProcessPaymentRequest;
import com.ticketbooking.payment.entity.Payment;
import com.ticketbooking.payment.entity.PaymentStatus;
import com.ticketbooking.payment.event.PaymentSuccessEvent;
import com.ticketbooking.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final BookingClient bookingClient;
    private final KafkaTemplate<String, PaymentSuccessEvent> kafkaTemplate;

    // Constructor (Yahan @Transactional NAHI hona chahiye)
    public PaymentService(PaymentRepository paymentRepository,
                          BookingClient bookingClient,
                          KafkaTemplate<String, PaymentSuccessEvent> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.bookingClient = bookingClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    // Method (Yahan @Transactional hona chahiye)
    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        BookingResponse booking;
        try {
            booking = bookingClient.getBooking(request.bookingId());
        } catch (Exception e) {
            log.error("Failed to fetch booking details for bookingId: {}", request.bookingId(), e);
            throw new IllegalArgumentException("Booking not found or booking service is unavailable");
        }

        java.math.BigDecimal paymentAmount = (request.amount() != null) ? request.amount() : booking.totalAmount();

        log.info("Processing payment for bookingId: {}, amount: {}", request.bookingId(), paymentAmount);

        if (!"PENDING".equalsIgnoreCase(booking.status())) {
            log.warn("Booking {} status is not PENDING (current status: {})", request.bookingId(), booking.status());
            throw new IllegalStateException("Only bookings in PENDING status can be paid");
        }

        if (paymentAmount != null && booking.totalAmount() != null && booking.totalAmount().compareTo(paymentAmount) != 0) {
            log.warn("Payment amount {} does not match booking total amount {}", paymentAmount, booking.totalAmount());
            throw new IllegalArgumentException("Payment amount does not match booking total amount");
        }

        String transactionId = "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        log.info("Simulated payment SUCCESS for bookingId: {} with transactionId: {}", request.bookingId(), transactionId);

        Payment payment = new Payment();
        payment.setBookingId(booking.id());
        payment.setUserId(booking.userId());
        payment.setAmount(paymentAmount);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(transactionId);

        Payment savedPayment = paymentRepository.save(payment);

        PaymentSuccessEvent successEvent = new PaymentSuccessEvent(
                booking.id(),
                booking.userId(),
                booking.eventId(),
                booking.seatId(),
                savedPayment.getAmount(),
                savedPayment.getTransactionId()
        );

        try {
            kafkaTemplate.send("payment-events-topic", booking.id().toString(), successEvent);
            log.info("Published PaymentSuccessEvent to payment-events-topic for bookingId: {}", booking.id());
        } catch (Exception e) {
            log.error("Failed to publish PaymentSuccessEvent to Kafka for bookingId: {}", booking.id(), e);
        }

        return new PaymentResponse(
                savedPayment.getId(),
                savedPayment.getBookingId(),
                savedPayment.getUserId(),
                savedPayment.getAmount(),
                savedPayment.getPaymentStatus().name(),
                savedPayment.getTransactionId(),
                savedPayment.getCreatedAt()
        );
    }
}