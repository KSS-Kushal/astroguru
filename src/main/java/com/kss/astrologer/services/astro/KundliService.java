package com.kss.astrologer.services.astro;

import com.kss.astrologer.request.KundliRequest;

public interface KundliService {
    String getAccessToken();
    Object getKundli(KundliRequest kundliRequest, String language);
    String getChart(KundliRequest kundliRequest, String chartType, String chartStyle, String language);
    Object getVimshottariDasha(KundliRequest kundliRequest, String dashaType, String language);
}
