package com.kss.astrologer.models;

import com.kss.astrologer.types.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notification_user", columnList = "userId"),
                @Index(name = "idx_notification_read", columnList = "isRead")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String title;
    private String message;

    // Deep link (frontend navigation)
    private String actionUrl;

    // bookingId, postId, chatId, etc.
    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;

    private boolean isRead = false;

    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();
}
