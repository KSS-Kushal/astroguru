package com.kss.astrologer.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HoroscopeVimshottariRequest extends HoroscopeBasicAstroRequest {
    private String dasha_type;
}
