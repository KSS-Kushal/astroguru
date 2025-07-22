package com.kss.astrologer.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kss.astrologer.models.WalletTransaction;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {

    Page<WalletTransaction> findByWalletId(UUID walletId, Pageable pageable);

    @Query("""
            SELECT wt FROM WalletTransaction wt
            WHERE wt.wallet.user.role = 'ADMIN'
            AND EXTRACT(YEAR FROM wt.timestamp) = :year
            """)
    List<WalletTransaction> findAllAdminTransactionsByYear(@Param("year") Integer year);

}
