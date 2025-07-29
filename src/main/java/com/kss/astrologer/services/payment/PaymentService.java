package com.kss.astrologer.services.payment;

import com.kss.astrologer.models.WalletTopup;
import com.razorpay.Order;

import java.util.UUID;

public interface PaymentService {
    public Order createOrder(Double amount, UUID userId);
    public boolean verifySignature(String payload, String signature);
    public void processWebhook(String payload);
    public void checkPaymentStatus(WalletTopup walletTopup);
}
