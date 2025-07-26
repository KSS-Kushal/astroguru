package com.kss.astrologer.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HoroscopeChartRequest extends HoroscopeBasicAstroRequest {
//    private String api_key;
//    private String full_name;
//    private int day;
//    private int month;
//    private int year;
//    private int hour;
//    private int min;
//    private int sec;
//    private String place;
//    private String gender;
//    private float lat;
//    private float lon;
//    private float tzone;
//    private String lan = "en"; // Optional
    private String planet_color = "#333333";
    private String sign_color = "#333333";
    private String line_color = "#FF0000";
    private String chart_color = "#F8FF85";
    private String chart_type = "east";
}
