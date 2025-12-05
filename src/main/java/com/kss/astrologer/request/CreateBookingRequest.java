package com.kss.astrologer.request;

import com.kss.astrologer.types.BookingType;
import com.kss.astrologer.types.SessionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateBookingRequest {
    @NotNull(message = "Astrologer ID cannot be null")
    private UUID astrologerId;
    @NotNull(message = "Appointment date cannot be null")
    private LocalDate appointmentDate;
    @Min(value = 2, message = "Minimum duration is 2 minutes")
    private int appointmentDuration;   // minutes
    private String reason;
    private BookingType bookingType;
    private SessionType sessionType;
}
