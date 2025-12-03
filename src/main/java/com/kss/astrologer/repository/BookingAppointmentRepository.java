package com.kss.astrologer.repository;

import com.kss.astrologer.models.BookingAppointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface BookingAppointmentRepository extends JpaRepository<BookingAppointment, UUID> {
    long countByAstrologer_IdAndAppointmentDate(UUID astrologerId, LocalDate appointmentDate);
    Page<BookingAppointment> findByAstrologer_IdOrUser_Id(UUID astrologerId, UUID userId, Pageable pageable);
}
