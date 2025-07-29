package com.kss.astrologer.services.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.models.User;
import com.kss.astrologer.models.WalletTopup;
import com.kss.astrologer.models.WalletTransaction;
import com.kss.astrologer.repository.WalletTopupRepository;
import com.kss.astrologer.services.UserService;
import com.kss.astrologer.services.WalletService;
import com.kss.astrologer.types.PaymentStatus;
import com.kss.astrologer.types.TransactionType;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Service
public class RazorpayService implements PaymentService {

    private final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    private final String keySecret = dotenv.get("RAZORPAY_KEY_SECRET");

    private static final Logger logger = LoggerFactory.getLogger(RazorpayService.class);

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletTopupRepository walletTopupRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Order createOrder(Double amount, UUID userId) {
        try {
            User user = userService.getById(userId);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount * 100); // Razorpay uses paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", UUID.randomUUID().toString());

            Order order = razorpayClient.orders.create(orderRequest);

            WalletTransaction transaction = walletService.topup(userId, amount);
            WalletTopup walletTopup = new WalletTopup();
            walletTopup.setUserId(userId);
            walletTopup.setWalletTransaction(transaction);
            walletTopup.setAmount(amount);
            walletTopup.setIsFirstTopUp(!user.getIsFirstTopUpDone());
            walletTopup.setOrderId(order.get("id"));
            walletTopup.setStatus(PaymentStatus.CREATED);

            walletTopupRepository.save(walletTopup);

            return order;
        } catch (Exception e) {
            throw new CustomException("Order creation failed: " + e.getMessage());
        }
    }

    @Override
    public boolean verifySignature(String payload, String signature) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(keySecret.getBytes(), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = Base64.getEncoder().encodeToString(hash);
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            throw new CustomException("Signature verification failed");
        }
    }

    @Override
    public void processWebhook(String payload) {
        System.out.println("Payload" + payload);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(payload);
            String event = root.get("event").asText();

            if ("payment.captured".equals(event)) {
                JsonNode payment = root.get("payload").get("payment").get("entity");
                String orderId = payment.get("order_id").asText();
                String paymentId = payment.get("id").asText();
                double amount = payment.get("amount").asDouble() / 100;

                // Update PaymentAttempt and Wallet
                WalletTopup walletTopup = walletTopupRepository.findByOrderId(orderId).orElseThrow(
                        () -> new CustomException("OrderId invalid")
                );

                walletTopup.setAmount(amount);
                walletTopup.setPaymentId(paymentId);
                walletTopup.setStatus(PaymentStatus.SUCCESS);

                walletTopup = walletTopupRepository.save(walletTopup);

                WalletTransaction transaction = walletTopup.getWalletTransaction();
                walletService.topup(transaction, amount, TransactionType.CREDIT, walletTopup.getIsFirstTopUp());
            } else if ("payment.failed".equals(event)) {
                JsonNode payment = root.get("payload").get("payment").get("entity");
                String orderId = payment.get("order_id").asText();
                String paymentId = payment.get("id").asText();
                double amount = payment.get("amount").asDouble() / 100;

                // Update PaymentAttempt and Wallet
                WalletTopup walletTopup = walletTopupRepository.findByOrderId(orderId).orElseThrow(
                        () -> new CustomException("OrderId invalid")
                );

                walletTopup.setAmount(amount);
                walletTopup.setPaymentId(paymentId);
                walletTopup.setStatus(PaymentStatus.FAILED);

                walletTopup = walletTopupRepository.save(walletTopup);

                WalletTransaction transaction = walletTopup.getWalletTransaction();
                walletService.topup(transaction, amount, TransactionType.FAILED, walletTopup.getIsFirstTopUp());
            } else {
                JsonNode payment = root.get("payload").get("payment").get("entity");
                String orderId = payment.get("order_id").asText();
                String paymentId = payment.get("id").asText();
                double amount = payment.get("amount").asDouble() / 100;

                // Update PaymentAttempt and Wallet
                WalletTopup walletTopup = walletTopupRepository.findByOrderId(orderId).orElseThrow(
                        () -> new CustomException("OrderId invalid")
                );

                walletTopup.setAmount(amount);
                walletTopup.setPaymentId(paymentId);
                walletTopup.setStatus(PaymentStatus.PENDING);

                walletTopupRepository.save(walletTopup);
            }
        } catch (JsonProcessingException e) {
            logger.error("Error to process webhook", e);
            throw new CustomException("Failed to process webhook");
        }
    }

    @Override
    public void checkPaymentStatus(WalletTopup walletTopup) {
        try {
            Payment razorpayPayment = razorpayClient.payments.fetch(walletTopup.getPaymentId());

            String status = razorpayPayment.get("status");
            if ("captured".equals(status)) {
                walletTopup.setStatus(PaymentStatus.SUCCESS);
                walletTopup = walletTopupRepository.save(walletTopup);

                WalletTransaction transaction = walletTopup.getWalletTransaction();
                walletService.topup(transaction, walletTopup.getAmount(), TransactionType.CREDIT, walletTopup.getIsFirstTopUp());
            } else if ("failed".equals(status)) {
                walletTopup.setStatus(PaymentStatus.FAILED);
                walletTopup = walletTopupRepository.save(walletTopup);

                WalletTransaction transaction = walletTopup.getWalletTransaction();
                walletService.topup(transaction, walletTopup.getAmount(), TransactionType.FAILED, walletTopup.getIsFirstTopUp());
            }
        } catch (RazorpayException e) {
            logger.error("Error to check Payment status of PaymentId: {}", walletTopup.getPaymentId(), e);
        }
    }
}
