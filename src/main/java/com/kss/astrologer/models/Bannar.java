package com.kss.astrologer.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "bannars")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bannar {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String imgUrl;
}
