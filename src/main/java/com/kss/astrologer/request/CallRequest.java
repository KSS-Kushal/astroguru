package com.kss.astrologer.request;

import com.kss.astrologer.types.SessionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallRequest {
    
    @NotNull(message = "Astrologer ID cannot be null")
    private UUID astrologerId;

    @Min(value = 5, message = "Minimum duration is 5 minutes")
    private int duration; // in minutes

    @NotNull(message = "Call type cannot be null")
    private SessionType type;
}
