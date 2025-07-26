package com.kss.astrologer.dto;

import lombok.Data;

@Data
public class KundliChartDto {
    private Short success;
    private ChartData data;

    @Data
    public static class ChartData {
        private String svg;
        private String data;
        private String base64_image;
    }
}
