package com.kss.astrologer.dto;

import com.kss.astrologer.models.BookingAppointment;
import com.kss.astrologer.types.BookingStatus;
import com.kss.astrologer.types.BookingType;
import com.kss.astrologer.types.SessionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingAppointmentDto {
    private UUID id;
    private UserDto user;
    private UserDto astrologer;
    private String reason;
    private LocalDate appointmentDate;
    private int appointmentDuration = 5;
    private Double totalCost = 0.0;
    private int otp;
    private BookingStatus status;
    private BookingType bookingType;
    private SessionType sessionType;
    private UUID chatSessionId;
    private UUID callSessionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BookingAppointmentDto(BookingAppointment appointment) {
        this.id = appointment.getId();
        this.user = new UserDto(appointment.getUser());
        this.astrologer = new UserDto(appointment.getAstrologer());
        this.appointmentDate = appointment.getAppointmentDate();
        this.appointmentDuration = appointment.getAppointmentDuration();
        this.reason = appointment.getReason();
        this.totalCost = appointment.getTotalCost();
        this.otp = appointment.getOtp();
        this.status = appointment.getStatus();
        this.bookingType = appointment.getBookingType();
        this.sessionType = appointment.getSessionType();
        this.chatSessionId = appointment.getChatSession() != null ? appointment.getChatSession().getId() : null;
        this.callSessionId = appointment.getCallSession() != null ? appointment.getCallSession().getId() : null;
        this.createdAt = appointment.getCreatedAt();
        this.updatedAt = appointment.getUpdatedAt();
    }
}
