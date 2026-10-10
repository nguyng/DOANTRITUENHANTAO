package com.ucop.hethongphanluongbenhvien.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tin nhắn trong chat — khớp bảng CHAT_MESSAGES trong database.sql.
 * Thuộc về một ChatSession, không trực tiếp link đến patient.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CHAT_MESSAGES", indexes = {
    @Index(name = "idx_chatmsg_session", columnList = "session_id"),
    @Index(name = "idx_chatmsg_created", columnList = "created_at")
})
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role", nullable = false, length = 10)
    private SenderRole senderRole;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 20)
    private MessageType messageType = MessageType.TEXT;

    /** URL ảnh BHYT nếu bệnh nhân gửi ảnh (khớp cột media_url trong SQL) */
    @Column(name = "media_url", length = 255)
    private String mediaUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = true)
    private LocalDateTime createdAt;

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum SenderRole {
        USER, BOT, SYSTEM
    }

    public enum MessageType {
        TEXT, IMAGE
    }
}

