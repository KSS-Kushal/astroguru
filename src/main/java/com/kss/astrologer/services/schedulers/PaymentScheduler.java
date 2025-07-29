package com.kss.astrologer.services.schedulers;

import com.kss.astrologer.models.WalletTopup;
import com.kss.astrologer.repository.WalletTopupRepository;
import com.kss.astrologer.services.payment.PaymentService;
import com.kss.astrologer.types.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PaymentScheduler.class);

    @Autowired
    private WalletTopupRepository walletTopupRepository;

    @Autowired
    private PaymentService paymentService;

    @Scheduled(cron = "0 0 1 * * ?") // Runs every day at 1 AM
    public void checkPendingPayments() {
        List<WalletTopup> pendingPayments = walletTopupRepository.findByStatus(PaymentStatus.PENDING);

        for (WalletTopup payment : pendingPayments) {
            try {
                paymentService.checkPaymentStatus(payment);
            } catch (Exception e) {
                logger.warn("Failed to check payment: {}", payment.getId(), e);
            }
        }
    }

    @Scheduled(cron = "0 0 2 * * *") // Runs every day at 2 AM
    @Transactional
    public void cleanupOldCreatedTopups() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        List<WalletTopup> oldTopups = walletTopupRepository
                .findByStatusAndCreatedAtBefore(PaymentStatus.CREATED, tenMinutesAgo);

        if (!oldTopups.isEmpty()) {
            logger.info("Deleting {} stale WalletTopup records", oldTopups.size());
            walletTopupRepository.deleteAll(oldTopups);
        } else {
            logger.info("No stale WalletTopup records found.");
        }
    }
}
