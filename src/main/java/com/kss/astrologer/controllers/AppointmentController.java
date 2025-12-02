package com.kss.astrologer.controllers;

import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.models.BookingAppointment;
import com.kss.astrologer.request.CreateBookingRequest;
import com.kss.astrologer.security.CustomUserDetails;
import com.kss.astrologer.services.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appointment")
public class AppointmentController {
    private final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    @Autowired
    private BookingService bookingService;

    @PostMapping("/booking")
    public ResponseEntity<Object> bookAppointment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @RequestBody CreateBookingRequest request) {
        BookingAppointment appointment = bookingService.bookAppointment(request, userDetails.getUserId());
        return ResponseHandler.responseBuilder(HttpStatus.CREATED, true, "Appointment booked successfully",
                "appointment", appointment);
    }
}
