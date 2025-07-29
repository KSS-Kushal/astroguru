package com.kss.astrologer.repository;

import com.kss.astrologer.models.WalletTopup;
import com.kss.astrologer.types.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletTopupRepository extends JpaRepository<WalletTopup, UUID> {
    Optional<WalletTopup> findByOrderId(String orderId);

    List<WalletTopup> findByStatus(PaymentStatus status);

    List<WalletTopup> findByStatusAndCreatedAtBefore(PaymentStatus paymentStatus, LocalDateTime tenMinutesAgo);
}
