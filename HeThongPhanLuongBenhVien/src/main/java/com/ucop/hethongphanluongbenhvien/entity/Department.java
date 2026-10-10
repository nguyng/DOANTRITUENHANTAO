package com.ucop.hethongphanluongbenhvien.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Khoa / Phòng ban của bệnh viện.
 * Mỗi khoa có một hàng đợi riêng, một mã code duy nhất,
 * và thông tin vị trí để chatbot chỉ dẫn đường cho bệnh nhân.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DEPARTMENTS", indexes = {
    @Index(name = "idx_dept_code",   columnList = "code",      unique = true),
    @Index(name = "idx_dept_active", columnList = "is_active")
})
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ── Thông tin cơ bản ─────────────────────────────────────────────────────

    /** Tên khoa — VD: "Khoa Tim Mạch" */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Mã khoa ngắn — dùng làm prefix số thứ tự.
     * VD: "TM" → Phiếu "TM-001", "CC" → "CC-001"
     */
    @Column(nullable = false, length = 20, unique = true)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ── Vị trí ────────────────────────────────────────────────────────────────

    /** Tòa nhà — VD: "Tòa A", "Tòa B" */
    @Column(length = 50)
    private String building;

    /** Số tầng */
    private Integer floor;

    /** Danh sách phòng — VD: "101–108" */
    @Column(name = "room_numbers", length = 50)
    private String roomNumbers;

    /**
     * Chỉ dẫn đường đi chi tiết cho chatbot.
     * VD: "Từ sảnh chính Tòa A, đi thẳng đến thang máy, lên Tầng 3, rẽ trái."
     */
    @Column(name = "location_guide", columnDefinition = "TEXT")
    private String locationGuide;

    // ── Cấu hình hàng đợi ────────────────────────────────────────────────────

    /** Prefix để tạo số thứ tự — VD: "NB", "CC", "TM" */
    @Builder.Default
    @Column(name = "ticket_prefix", length = 10)
    private String ticketPrefix = "NB";

    /** Thời gian khám trung bình mỗi bệnh nhân (phút) — để tính thời gian chờ dự kiến */
    @Builder.Default
    @Column(name = "avg_consult_minutes")
    private Integer avgConsultMinutes = 15;

    // ── Trạng thái ────────────────────────────────────────────────────────────

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    // ── Metadata ─────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = true)
    private LocalDateTime createdAt;
}

