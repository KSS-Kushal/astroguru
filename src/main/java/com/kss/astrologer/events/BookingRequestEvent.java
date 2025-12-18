package com.kss.astrologer.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class BookingRequestEvent {
    private UUID bookingId;
    private UUID userId;
    private UUID astrologerId;
}
