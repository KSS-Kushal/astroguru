package com.kss.astrologer.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypingIndicator {
    private UUID sessionId;
    private UUID senderId;
    private UUID receiverId;
    private boolean typing;
    @Override
    public String toString() {
        return "{sessionId:" + sessionId + ", senderId:" + senderId + ", receiverId:" + receiverId
                + ", typing:" + typing + "}";
    }

    
}
