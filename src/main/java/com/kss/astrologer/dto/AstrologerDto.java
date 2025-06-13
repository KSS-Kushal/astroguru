package com.kss.astrologer.dto;

import java.util.UUID;

import com.kss.astrologer.models.AstrologerDetails;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AstrologerDto {

    private UUID id;
    private UserDto user;

    private String expertise;

    private int experienceYears;

    private Double pricePerMinuteChat;
    private Double pricePerMinuteVoice;
    private Double pricePerMinuteVideo;

    private boolean isBlocked;

    public AstrologerDto(AstrologerDetails astrologerDetails) {
        this.id = astrologerDetails.getId();
        this.user = new UserDto(astrologerDetails.getUser());
        this.expertise = astrologerDetails.getExpertise();
        this.experienceYears = astrologerDetails.getExperienceYears();
        this.pricePerMinuteChat = astrologerDetails.getPricePerMinuteChat();
        this.pricePerMinuteVoice = astrologerDetails.getPricePerMinuteVoice();
        this.pricePerMinuteVideo = astrologerDetails.getPricePerMinuteVideo();
        this.isBlocked = astrologerDetails.isBlocked();
    }
}
