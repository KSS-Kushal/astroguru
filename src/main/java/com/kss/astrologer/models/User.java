package com.kss.astrologer.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import com.kss.astrologer.types.Gender;
import com.kss.astrologer.types.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    private LocalDate birthDate;
    private LocalTime birthTime;
    private String birthPlace;
    private Double latitude;
    private Double longitude;

    private String imgUri;

    @Column(unique = true, nullable = false)
    private String mobile;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role; // USER, ASTROLOGER, ADMIN

    private boolean isFreeChatUsed; // Only for USER
    private Boolean isFirstTopUpDone;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Wallet wallet;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

