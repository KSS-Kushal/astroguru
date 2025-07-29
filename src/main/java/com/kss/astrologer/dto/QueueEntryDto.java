package com.kss.astrologer.dto;

import com.kss.astrologer.models.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QueueEntryDto extends ChatQueueEntry {
    private UserDto user;

    public QueueEntryDto(ChatQueueEntry entry, User user) {
        super(entry.getUserId(), entry.getRequestedMinutes(), entry.getSessionType());
        this.user = new UserDto(user);
    }
}
