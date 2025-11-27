package com.kss.astrologer.services;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import com.kss.astrologer.services.sms.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.models.OtpVerification;
import com.kss.astrologer.repository.OtpVerificationRepository;

@Service
public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    @Autowired
    private OtpVerificationRepository otpRepository;

    @Autowired
    private SmsService smsService;

    public int generateOtp() {
        return new Random().nextInt(9000) + 1000;
    }

    public String sendOtp(String mobile) {
        String otp = String.valueOf(generateOtp());
        otpRepository.save(OtpVerification.builder()
                .mobile(mobile)
                .otp(otp)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build());

        // TODO: Integrate with SMS API - Done
        logger.info("OTP for {}: {}", mobile, otp);
        String sms = smsService.sendSms(mobile, otp);
        logger.info(sms);
        return otp;
    }

    public boolean verifyOtp(String mobile, String otp) {
        Optional<OtpVerification> otpEntity = otpRepository.findTopByMobileOrderByCreatedAtDesc(mobile);
        if(otpEntity.isPresent() && otpEntity.get().getExpiresAt().isBefore(LocalDateTime.now()))
            throw new CustomException(HttpStatus.UNAUTHORIZED, "OTP has expired. Please request a new OTP.");
        return otpEntity.isPresent()
                && otpEntity.get().getOtp().equals(otp)
                && otpEntity.get().getExpiresAt().isAfter(LocalDateTime.now());
    }

}
