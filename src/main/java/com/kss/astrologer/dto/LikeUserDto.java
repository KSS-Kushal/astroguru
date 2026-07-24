package com.kss.astrologer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class LikeUserDto {
    private UUID id;
    private String name;
}
