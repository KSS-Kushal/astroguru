package com.kss.astrologer.request;

import lombok.Data;

import java.util.UUID;

@Data
public class ActiveSessionRequest {
    private UUID astrologerId;
}
