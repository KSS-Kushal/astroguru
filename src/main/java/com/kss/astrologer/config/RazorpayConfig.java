package com.kss.astrologer.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {

    private final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    private final String keyId = dotenv.get("RAZORPAY_KEY_ID");
    private final String keySecret = dotenv.get("RAZORPAY_KEY_SECRET");

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(keyId, keySecret);
    }
}
