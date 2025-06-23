package com.kss.astrologer.services.astro;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.kss.astrologer.request.KundliRequest;

@Service
public class KundliServiceIml implements KundliService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${prokerala.api.client-id}")
    private String clientId;

    @Value("${prokerala.api.client-secret}")
    private String clientSecret;

    private final String tokenUrl = "https://api.prokerala.com/token";
    private final String kundliUrl = "https://api.prokerala.com/v2/astrology/kundli";

    @Override
    public String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Set form params
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        if (response.getStatusCode().is2xxSuccessful()) {
            return (String) response.getBody().get("access_token");
        } else {
            throw new RuntimeException("Failed to get access token");
        }
    }

    @Override
    public Object getKundli(KundliRequest kundliRequest) {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String coordinates = kundliRequest.getLatitude() + "," + kundliRequest.getLongitude();
        String datetime = getFormattedDateTime(kundliRequest); // Instead of manually formatting

        String url = kundliUrl + "?ayanamsa=1"
            + "&coordinates=" + coordinates
            + "&datetime=" + encode(datetime);
        

            URI uri = URI.create(url);
        // uri = uri.replace("+", "%2B");
        System.out.println("Request URL: " + uri);
        System.out.println("date time: " + datetime);

        ResponseEntity<Object> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                Object.class);

        return response.getBody();

    }

    private String encode(String value) {
        return value.replace("+", "%2B");
    }

    private String getFormattedDateTime(KundliRequest req) {
        // Combine date and time
        LocalDate birthDate = req.getBirthDate(); // e.g., 1990-08-15
        LocalTime birthTime = req.getBirthTime(); // e.g., 06:30:00

        // Combine into LocalDateTime
        LocalDateTime localDateTime = LocalDateTime.of(birthDate, birthTime);

        // Convert to IST timezone (Asia/Kolkata)
        ZoneId zoneId = ZoneId.of("Asia/Kolkata");
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);

        // Format with seconds and timezone offset
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        // Format in ISO 8601 with timezone offset
        return zonedDateTime.toOffsetDateTime().format(formatter); // e.g., 1990-08-15T06:30:00+05:30
    }
}
