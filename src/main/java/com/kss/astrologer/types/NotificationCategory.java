package com.kss.astrologer.types;

public enum NotificationCategory {
    BROADCAST,     // post → all users / followers
    DIRECT,        // booking, like, comment
    CHAT,          // message
    SILENT         // background update
}
