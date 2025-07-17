package com.kss.astrologer.request;

import lombok.Data;

@Data
public class HoroscopeRequest {
    private String api_key;
    private String sign;
    private Integer day;
    private Integer month;
    private Integer year;
    private Double tzone;
    private String lan;
}
