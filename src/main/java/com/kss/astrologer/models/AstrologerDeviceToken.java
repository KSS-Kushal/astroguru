package com.kss.astrologer.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "astrologer_device_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AstrologerDeviceToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String deviceToken;

    private LocalDateTime createdAt = LocalDateTime.now();
}
