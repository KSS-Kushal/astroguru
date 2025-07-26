package com.kss.astrologer.request;

import lombok.Data;

@Data
public class MonthlyHoroscopeRequest {
    private String api_key;
    private String sign;
    private String month;
    private Double tzone;
    private String lan;
}
