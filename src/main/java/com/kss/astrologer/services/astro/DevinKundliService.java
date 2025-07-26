package com.kss.astrologer.services.astro;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kss.astrologer.dto.HouseData;
import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.request.HoroscopeBasicAstroRequest;
import com.kss.astrologer.request.HoroscopeChartRequest;
import com.kss.astrologer.request.HoroscopeVimshottariRequest;
import com.kss.astrologer.request.KundliRequest;
import com.kss.astrologer.utils.EastIndianChartRenderer;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

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
    private final String vimshottariUrl = "https://astroapi-3.divineapi.com/indian-api/v1/vimshottari-dasha";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String getAccessToken() {
        return apiToken;
    }

    @Cacheable(value = "kundli", key = "#kundliRequest.latitude + '-' + #kundliRequest.longitude + '-' + #kundliRequest.birthDate + '-' + #kundliRequest.birthTime + '-' + #language" )
    @Override
    public Object getKundli(KundliRequest kundliRequest, String language) {
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

        HoroscopeBasicAstroRequest body = new HoroscopeBasicAstroRequest();
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
        body.setLan(language);

        HttpEntity<HoroscopeBasicAstroRequest> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Object> response = restTemplate.postForEntity(
                kundliUrl,
                entity,
                Object.class
        );

        return response.getBody();
    }

    @Cacheable(value = "chart", key = "#kundliRequest.latitude + '-' + #kundliRequest.longitude + '-' + #kundliRequest.birthDate + '-' + #kundliRequest.birthTime + '-' + #chartType + '-' + #chartStyle + '-' + #language")
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
        body.setLan(chartStyle.equals("east")?"en":language);
        body.setChart_type(chartStyle.equals("east")?"north":chartStyle);

        HttpEntity<HoroscopeChartRequest> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

//        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//            System.out.println(response.getBody() + "response");
//            if (response instanceof KundliChartDto ) {
//                if(response.getBody().getData() == null) throw new CustomException("Failed to fetch chart svg");
//                return response.getBody().getData().getSvg();
//            }
//        }
        try {
            JsonNode root = objectMapper.readTree(response.getBody());

            if(language.equals("bn") && chartStyle.equals("east")) {
                // Step 1: get the JSON string from the `data.data` field
                JsonNode dataString = root.path("data").path("data");
                // Step 2: Convert JSON string to Map<String, HouseData>
                Map<String, HouseData> chartData = objectMapper.convertValue(
                        dataString,
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, HouseData.class)
                );

                String svg = EastIndianChartRenderer.generateSvg(chartData, true);
                return svg;
            }

            String svg = root.path("data").path("svg").asText();
            return svg;

        } catch (Exception e) {
            throw new CustomException("Failed to fetch chart svg");
        }

//        throw new CustomException("Failed to fetch chart svg");
    }

    @Override
    @Cacheable(value = "vimshottariDasha", key = "#kundliRequest.latitude + '-' + #kundliRequest.longitude + '-' + #kundliRequest.birthDate + '-' + #kundliRequest.birthTime + '-' + #dashaType + '-' + #language")
    public Object getVimshottariDasha(KundliRequest kundliRequest, String dashaType, String language) {
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
        HoroscopeVimshottariRequest body = new HoroscopeVimshottariRequest();
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
        body.setLan(language);
        body.setDasha_type(dashaType);

        HttpEntity<HoroscopeVimshottariRequest> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Object> response = restTemplate.postForEntity(vimshottariUrl, entity, Object.class);
        return response.getBody();
    }
}
