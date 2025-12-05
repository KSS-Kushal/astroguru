package com.kss.astrologer.models;

import com.kss.astrologer.types.BookingStatus;
import com.kss.astrologer.types.BookingType;
import com.kss.astrologer.types.SessionType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "booking_appointments")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingAppointment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "astrologer_id")
    private User astrologer;

    private String reason;
    private LocalDate appointmentDate;
    private int appointmentDuration;
    private Double totalCost;

    private int otp;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;
    private BookingType bookingType = BookingType.ONLINE;
    @Enumerated(EnumType.STRING)
    private SessionType sessionType;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
