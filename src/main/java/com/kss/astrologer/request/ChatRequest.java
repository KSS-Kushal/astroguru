package com.kss.astrologer.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest {
    
    @NotNull(message = "Astrologer ID cannot be null")
    private UUID astrologerId;

    @Min(value = 5, message = "Minimum duration is 5 minutes")
    private int duration; // in minutes
}
