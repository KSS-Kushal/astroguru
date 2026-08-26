package com.kss.astrologer.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class CallStartedEvent extends ApplicationEvent {
    private final UUID senderId;
    private final String senderName;
    private final UUID receiverId;
    private final String code;
    private final String sessionType;
    public CallStartedEvent(Object source, UUID senderId, String senderName, UUID receiverId, String code,
                            String sessionType) {
        super(source);
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.code = code;
        this.sessionType = sessionType;
    }
}
