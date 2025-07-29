package com.kss.astrologer.controllers;

import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.request.TopupRequest;
import com.kss.astrologer.security.CustomUserDetails;
import com.kss.astrologer.services.payment.PaymentService;
import com.razorpay.Order;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/topup")
    public ResponseEntity<Object> walletTopUp(@RequestBody @Valid TopupRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Order order = paymentService.createOrder(request.getAmount(), userDetails.getUserId());
        System.out.println("Controller " + order);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Payment Initiate", "order", order.toString());
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("X-Razorpay-Signature") String signature) {
        System.out.println("Webhook" + payload);
        boolean verified = paymentService.verifySignature(payload, signature);
//        if (!verified) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");

        paymentService.processWebhook(payload);
        return ResponseEntity.ok("Webhook received");
    }
}
