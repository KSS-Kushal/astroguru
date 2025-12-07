package com.kss.astrologer.controllers;

import com.kss.astrologer.dto.BookingAppointmentDto;
import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.models.BookingAppointment;
import com.kss.astrologer.request.CreateBookingRequest;
import com.kss.astrologer.request.CreateSessionRequest;
import com.kss.astrologer.request.UpdateAppointmentStatusRequest;
import com.kss.astrologer.security.CustomUserDetails;
import com.kss.astrologer.services.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    @GetMapping
    public ResponseEntity<Object> getAllBookedAppointment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                          @RequestParam(defaultValue = "1", required = false) Integer page,
                                                          @RequestParam(defaultValue = "10", required = false) Integer size) {
        Page<BookingAppointmentDto> appointments = bookingService.getAllBookedAppointment(userDetails.getUserId(),
                page, size);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Appointment fetched successfully",
                "appointments", appointments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getAppointmentById(@PathVariable UUID id) {
        BookingAppointmentDto appointment = bookingService.getAppointmentById(id);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Appointment fetched successfully",
                "appointment", appointment);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateAppointmentStatus(@PathVariable UUID id,
                                                          @RequestBody UpdateAppointmentStatusRequest body) {
        BookingAppointmentDto appointment = bookingService.updateStatus(id, body.getStatus(), body.getOtp());
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Appointment status updated successfully",
                "appointment", appointment);
    }

    @PatchMapping("/{id}/session")
    public ResponseEntity<Object> createSession(@PathVariable UUID id,
                                                @RequestBody CreateSessionRequest body) {
        BookingAppointmentDto appointment = bookingService.createChatSession(id, body.getType());
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Appointment session created successfully",
                "appointment", appointment);
    }
}
