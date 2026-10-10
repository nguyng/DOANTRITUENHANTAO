package com.ucop.hethongphanluongbenhvien.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Phiên chat — khớp bảng CHAT_SESSIONS trong database.sql.
 * Mỗi lần bệnh nhân mở trang /chat là một session mới.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CHAT_SESSIONS", indexes = {
    @Index(name = "idx_chatsession_user",    columnList = "user_id"),
    @Index(name = "idx_chatsession_started", columnList = "started_at")
})
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt;

    /** Null nếu session chưa kết thúc */
    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    /** Ticket được tạo ra từ session này (có thể null nếu bệnh nhân không xác nhận) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;
}

