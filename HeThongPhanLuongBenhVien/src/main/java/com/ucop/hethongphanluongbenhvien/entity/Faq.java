package com.ucop.hethongphanluongbenhvien.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Câu hỏi thường gặp (FAQ) — Admin cấu hình để Chatbot trả lời tự động.
 * Ví dụ: "Giờ làm việc?", "Chi phí khám?", "Cần mang giấy tờ gì?"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "FAQS", indexes = {
    @Index(name = "idx_faq_active",    columnList = "is_active"),
    @Index(name = "idx_faq_category",  columnList = "category"),
    @Index(name = "idx_faq_dept",      columnList = "department_id")
})
public class Faq {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ── Nội dung ──────────────────────────────────────────────────────────────

    /** Câu hỏi — dùng để matching với câu hỏi bệnh nhân */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    /** Câu trả lời tự động của bot */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    /**
     * Các từ khóa trigger (phân cách bằng dấu phẩy).
     * VD: "giờ, mở cửa, làm việc, hoạt động"
     */
    @Column(columnDefinition = "TEXT")
    private String keywords;

    /** Danh mục: GENERAL, PAYMENT, DOCUMENT, INSURANCE, APPOINTMENT */
    @Column(length = 30)
    private String category;

    /** Khoa liên quan (null = áp dụng toàn viện) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // ── Hiển thị & sắp xếp ────────────────────────────────────────────────────

    /** Thứ tự hiển thị trong danh sách gợi ý */
    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /** Có hiển thị nút quick-reply trong chat không */
    @Builder.Default
    @Column(name = "show_as_quick_reply")
    private Boolean showAsQuickReply = false;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    // ── Thống kê ─────────────────────────────────────────────────────────────

    /** Số lần câu hỏi này được bệnh nhân hỏi */
    @Builder.Default
    @Column(name = "hit_count")
    private Long hitCount = 0L;

    // ── Metadata ─────────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = true)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;
}

