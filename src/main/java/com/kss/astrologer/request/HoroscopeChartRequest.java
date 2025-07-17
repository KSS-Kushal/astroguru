package com.kss.astrologer.request;

import lombok.Data;

@Data
public class HoroscopeChartRequest {
    private String api_key;
    private String full_name;
    private int day;
    private int month;
    private int year;
    private int hour;
    private int min;
    private int sec;
    private String place;
    private String gender;
    private float lat;
    private float lon;
    private float tzone;
    private String lan = "en"; // Optional
    private String planet_color;
    private String sign_color;
    private String line_color;
    private String chart_color;
    private String chart_type = "east";
}
