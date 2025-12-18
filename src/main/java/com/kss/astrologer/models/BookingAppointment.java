package com.kss.astrologer.models;

import com.kss.astrologer.types.BookingStatus;
import com.kss.astrologer.types.BookingType;
import com.kss.astrologer.types.SessionType;
import jakarta.persistence.*;
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
    @Enumerated(EnumType.STRING)
    private BookingType bookingType = BookingType.ONLINE;
    @Enumerated(EnumType.STRING)
    private SessionType sessionType;

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    private ChatSession chatSession;
    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    private CallSession callSession;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
