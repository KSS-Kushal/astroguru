package com.kss.astrologer.controllers;

import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.services.astro.HoroscopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/horoscope")
public class HoroscopeController {

    @Autowired
    private HoroscopeService horoscopeService;

    @GetMapping("/daily")
    public ResponseEntity<Object> getDailyHoroscope(@RequestParam String sign,
                                                    @RequestParam Integer day,
                                                    @RequestParam Integer month,
                                                    @RequestParam Integer year,
                                                    @RequestParam(required = false, defaultValue = "5.5") Double tzone,
                                                    @RequestParam(required = false, defaultValue = "en") String lan) {
        Object response = horoscopeService.getDailyHoroscope(sign, day, month, year, tzone, lan);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Fetched Daily Horoscope", response);
    }

    @GetMapping("/weekly")
    public ResponseEntity<Object> getWeeklyHoroscope(
            @RequestParam String sign,
            @RequestParam(defaultValue = "current", required = false) String week,
            @RequestParam(defaultValue = "5.5", required = false) Double tzone,
            @RequestParam(defaultValue = "en", required = false) String lan
    ) {
        Object response = horoscopeService.getWeeklyHoroscope(sign, week, tzone, lan);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Fetched Weekly Horoscope", response);
    }

    @GetMapping("/monthly")
    public ResponseEntity<Object> getMonthlyHoroscope(
            @RequestParam String sign,
            @RequestParam(defaultValue = "current", required = false) String month,
            @RequestParam(defaultValue = "5.5", required = false) Double tzone,
            @RequestParam(defaultValue = "en", required = false) String lan
    ) {
        Object response = horoscopeService.getMonthlyHoroscope(sign, month, tzone, lan);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Fetched Monthly Horoscope", response);
    }
}
