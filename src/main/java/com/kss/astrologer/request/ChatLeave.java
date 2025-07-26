package com.kss.astrologer.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatLeave {
    private UUID userId;
    private UUID astrologerId;
}
