package com.kss.astrologer.request;

import lombok.Data;

@Data
public class WeeklyHoroscopeRequest {
    private String api_key;
    private String sign;
    private String week;
    private Double tzone;
    private String lan;
}
