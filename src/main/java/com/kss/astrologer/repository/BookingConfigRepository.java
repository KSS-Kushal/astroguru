package com.kss.astrologer.repository;

import com.kss.astrologer.models.BookingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingConfigRepository extends JpaRepository<BookingConfig, UUID> {
    Optional<BookingConfig> findByAstrologer_Id(UUID astrologerId);
}
