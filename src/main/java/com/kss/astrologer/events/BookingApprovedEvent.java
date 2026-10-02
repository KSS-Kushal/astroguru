package com.kss.astrologer.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class BookingApprovedEvent {
    private UUID bookingId;
    private UUID userId;
}
