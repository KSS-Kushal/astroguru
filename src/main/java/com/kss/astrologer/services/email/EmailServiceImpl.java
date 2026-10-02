package com.kss.astrologer.services.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService{

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Astrosevaa Login OTP");
            message.setText(
                    "Your OTP for Astrosevaa login is: " + otp +
                            "\nThis OTP is valid for 10 minutes."
            );
            mailSender.send(message);
        } catch (Exception e) {
            logger.error("Error to sending otp on email ({}), error: {}", to, e.getMessage());
        }
    }
}
