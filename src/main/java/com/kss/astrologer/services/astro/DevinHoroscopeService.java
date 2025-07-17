package com.kss.astrologer.services.astro;

import com.kss.astrologer.request.HoroscopeRequest;
import com.kss.astrologer.request.MonthlyHoroscopeRequest;
import com.kss.astrologer.request.WeeklyHoroscopeRequest;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DevinHoroscopeService implements HoroscopeService {

    private final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    private final String apiKey = dotenv.get("DEVIN_API_KEY");
    private final String apiToken = dotenv.get("DEVIN_API_TOKEN");

    private final String dailyHoroscopeUrl = "https://astroapi-5-translator.divineapi.com/api/v2/daily-horoscope";
    private final String weeklyHoroscopeUrl = "https://astroapi-5-translator.divineapi.com/api/v3/weekly-horoscope";
    private final String monthlyHoroscopeUrl = "https://astroapi-5-translator.divineapi.com/api/v3/monthly-horoscope";

    @Autowired
    private RestTemplate restTemplate;

    @Cacheable(value = "dailyHoroscope", key = "#sign + '-' + #day + '-' + #month + '-' + #year + '-' + #tzone + '-' + #lan")
    @Override
    public Object getDailyHoroscope(String sign, Integer day, Integer month, Integer year, Double tzone, String lan) {
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        // Prepare body
        HoroscopeRequest request = new HoroscopeRequest();
        request.setApi_key(apiKey);
        request.setSign(sign);
        request.setDay(day);
        request.setMonth(month);
        request.setYear(year);
        request.setTzone(tzone);
        request.setLan(lan); // e.g., "hi", "pt", "fr", etc.

        HttpEntity<HoroscopeRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                dailyHoroscopeUrl,
                HttpMethod.POST,
                entity,
                Object.class
        );

        return response.getBody();
    }

    @Cacheable(value = "weeklyHoroscope", key = "#sign + '-' + #week + '-' + #tzone + '-' + #lan + '-weekly'")
    @Override
    public Object getWeeklyHoroscope(String sign, String week, Double tzone, String lan) {
        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        // Request body
        WeeklyHoroscopeRequest request = new WeeklyHoroscopeRequest();
        request.setApi_key(apiKey);
        request.setSign(sign);       // e.g., "Aries"
        request.setWeek(week);       // e.g., "current", "prev", or "next"
        request.setTzone(tzone);     // e.g., 5.5
        request.setLan(lan);         // e.g., "en", "hi", etc.

        HttpEntity<WeeklyHoroscopeRequest> entity = new HttpEntity<>(request, headers);

        // API call
        ResponseEntity<Object> response = restTemplate.exchange(
                weeklyHoroscopeUrl,
                HttpMethod.POST,
                entity,
                Object.class
        );

        return response.getBody();
    }

    @Cacheable(value = "monthlyHoroscope", key = "#sign + '-' + #month + '-' + #tzone + '-' + #lan + '-monthly'")
    @Override
    public Object getMonthlyHoroscope(String sign, String month, Double tzone, String lan) {
        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);


        // Body
        MonthlyHoroscopeRequest request = new MonthlyHoroscopeRequest();
        request.setApi_key(apiKey);
        request.setSign(sign);           // e.g., "Aries"
        request.setMonth(month);         // "current", "prev", "next"
        request.setTzone(tzone);         // e.g., 5.5
        request.setLan(lan);             // e.g., "en", "hi", "bn"

        HttpEntity<MonthlyHoroscopeRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                monthlyHoroscopeUrl,
                HttpMethod.POST,
                entity,
                Object.class
        );

        return response.getBody();
    }
}
