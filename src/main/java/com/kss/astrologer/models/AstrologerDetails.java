package com.kss.astrologer.models;

import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user; // Must be role=ASTROLOGER

    @Column(columnDefinition = "TEXT")
    private String about;
    
    private String expertise;
    private int experienceYears;
    private String languages; // Coma separated list of languages
    private String imgUri;

    private Double pricePerMinuteChat;
    private Double pricePerMinuteVoice;
    private Double pricePerMinuteVideo;

    private boolean isBlocked;
}

