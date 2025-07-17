package com.kss.astrologer.services.astro;

import com.kss.astrologer.dto.KundliChartDto;
import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.request.HoroscopeChartRequest;
import com.kss.astrologer.request.KundliRequest;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DevinKundliService implements KundliService{

    private final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    private final String apiKey = dotenv.get("DEVIN_API_KEY");
    private final String apiToken = dotenv.get("DEVIN_API_TOKEN");

    private final String chartUrl = "https://astroapi-3.divineapi.com/indian-api/v1/horoscope-chart";
    private final String kundliUrl = "https://astroapi-3.divineapi.com/indian-api/v2/basic-astro-details";

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String getAccessToken() {
        return apiToken;
    }

    @Cacheable(value = "kundli", key = "#kundliRequest.latitude + '-' + #kundliRequest.longitude + '-' + #kundliRequest.birthDate + '-' + #kundliRequest.birthTime")
    @Override
    public Object getKundli(KundliRequest kundliRequest, String languageck) {
        int day = kundliRequest.getBirthDate().getDayOfMonth();
        int month = kundliRequest.getBirthDate().getMonthValue();
        int year = kundliRequest.getBirthDate().getYear();
        int hour = kundliRequest.getBirthTime().getHour();
        int minute = kundliRequest.getBirthTime().getMinute();
        int second = kundliRequest.getBirthTime().getSecond();

        double timezone = 5.5;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        HoroscopeChartRequest body = new HoroscopeChartRequest();
        body.setApi_key(apiKey);
        body.setFull_name(kundliRequest.getName());
        body.setGender(kundliRequest.getGender().name().toLowerCase());
        body.setDay(day);
        body.setMonth(month);
        body.setYear(year);
        body.setHour(hour);
        body.setMin(minute);
        body.setSec(second);
        body.setPlace(kundliRequest.getBirthPlace());
        body.setLat(kundliRequest.getLatitude().floatValue());
        body.setLon(kundliRequest.getLongitude().floatValue());
        body.setTzone((float) timezone);
        body.setLan("en");

        HttpEntity<HoroscopeChartRequest> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Object> response = restTemplate.postForEntity(
                kundliUrl,
                entity,
                Object.class
        );

        return response.getBody();
    }

    @Cacheable(value = "chart", key = "#kundliRequest.latitude + '-' + #kundliRequest.longitude + '-' + #kundliRequest.birthDate + '-' + #kundliRequest.birthTime + '-' + #chartType + '-' + #chartStyle")
    @Override
    public String getChart(KundliRequest kundliRequest, String chartType, String chartStyle, String language) {
        String url = chartUrl + "/" + chartType;

        // Convert LocalDate and LocalTime to parts
        int day = kundliRequest.getBirthDate().getDayOfMonth();
        int month = kundliRequest.getBirthDate().getMonthValue();
        int year = kundliRequest.getBirthDate().getYear();
        int hour = kundliRequest.getBirthTime().getHour();
        int minute = kundliRequest.getBirthTime().getMinute();
        int second = kundliRequest.getBirthTime().getSecond();

        double timezone = 5.5;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        // Prepare body
        HoroscopeChartRequest body = new HoroscopeChartRequest();
        body.setApi_key(apiKey);
        body.setFull_name(kundliRequest.getName());
        body.setGender(kundliRequest.getGender().name().toLowerCase());
        body.setDay(day);
        body.setMonth(month);
        body.setYear(year);
        body.setHour(hour);
        body.setMin(minute);
        body.setSec(second);
        body.setPlace(kundliRequest.getBirthPlace());
        body.setLat(kundliRequest.getLatitude().floatValue());
        body.setLon(kundliRequest.getLongitude().floatValue());
        body.setTzone((float) timezone);
        body.setLan("en");
        body.setChart_type(chartType);

        HttpEntity<HoroscopeChartRequest> entity = new HttpEntity<>(body, headers);

        ResponseEntity<KundliChartDto> response = restTemplate.postForEntity(url, entity, KundliChartDto.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().getData().getSvg();
        }

        throw new CustomException("Failed to fetch chart svg");
    }
}
