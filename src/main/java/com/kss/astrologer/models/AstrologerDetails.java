package com.kss.astrologer.models;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "astrologer_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AstrologerDetails {
    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user; // Must be role=ASTROLOGER

    private String expertise;

    private int experienceYears;

    private BigDecimal pricePerMinuteChat;
    private BigDecimal pricePerMinuteVoice;
    private BigDecimal pricePerMinuteVideo;

    private boolean isApproved;
}

