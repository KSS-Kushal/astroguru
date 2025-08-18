package com.kss.astrologer.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAstrologerRequest {
    private String name;

    private String expertise;

    private String about;
    private Integer experienceYears = 0;
    private String languages; // Coma separated list of languages

    private Double pricePerMinuteChat = 0.0;
    private Double pricePerMinuteVoice = 0.0;
    private Double pricePerMinuteVideo = 0.0;
}
