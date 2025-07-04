package com.kss.astrologer.services.astro;

import com.kss.astrologer.request.KundliRequest;

public interface KundliService {
    String getAccessToken();
    Object getKundli(KundliRequest kundliRequest);
    String getChart(KundliRequest kundliRequest, String chartType, String chartStyle);
}
