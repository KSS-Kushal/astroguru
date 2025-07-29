package com.kss.astrologer.services.sms;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MSG91SmsService implements SmsService {
    private final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    private final String authKey = dotenv.get("SMS_AUTH_KEY");
    private final String senderId = dotenv.get("SMS_SENDER_ID");
    private final String templateId = dotenv.get("SMS_TEMPLATE_ID");

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String sendSms(String phoneNumber, String otp) {
        String url = "https://control.msg91.com/api/v5/flow";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authkey", authKey);

        Map<String, Object> body = new HashMap<>();
        body.put("template_id", templateId);
        body.put("short_url", "0"); // optional
        body.put("recipients", List.of(
                Map.of(
                        "mobiles", "91" + phoneNumber,
                        "OTP", otp
                )
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return response.getBody();
    }
}
