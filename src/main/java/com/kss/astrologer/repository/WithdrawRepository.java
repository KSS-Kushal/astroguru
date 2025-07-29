package com.kss.astrologer.repository;

import com.kss.astrologer.models.Withdraw;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WithdrawRepository extends JpaRepository<Withdraw, UUID> {
    Page<Withdraw> findByIsApproved(Boolean isApproved, Pageable pageable);
}
