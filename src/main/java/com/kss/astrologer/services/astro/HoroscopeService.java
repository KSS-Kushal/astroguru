package com.kss.astrologer.services.astro;

public interface HoroscopeService {
    Object getDailyHoroscope(String sign, Integer day, Integer month, Integer year, Double tzone, String lan);
    Object getWeeklyHoroscope(String sign, String week, Double tzone, String lan);
    Object getMonthlyHoroscope(String sign, String month, Double tzone, String lan);
}
