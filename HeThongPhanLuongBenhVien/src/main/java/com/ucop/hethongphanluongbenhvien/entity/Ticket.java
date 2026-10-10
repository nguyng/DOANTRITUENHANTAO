package com.ucop.hethongphanluongbenhvien.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Phiếu khám điện tử — khớp với bảng QUEUE_TICKETS trong database.sql.
 *
 * Mức ưu tiên đã được nâng cấp từ NORMAL/MEDIUM/EMERGENCY (3 mức cũ)
 * lên thang M1–M5 (5 mức — chuẩn triage quốc tế).
 * Cần cập nhật database.sql theo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "QUEUE_TICKETS", indexes = {
    @Index(name = "idx_tickets_patient",  columnList = "patient_id"),
    @Index(name = "idx_tickets_dept",     columnList = "department_id"),
    @Index(name = "idx_tickets_status",   columnList = "status"),
    @Index(name = "idx_tickets_priority", columnList = "priority"),
    @Index(name = "idx_tickets_created",  columnList = "created_at")
})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    /**
     * Số thứ tự hiển thị — VD: "NB-007", "CC-001".
     * Dùng String thay INT để dễ format theo prefix của khoa.
     */
    @Column(name = "ticket_number", nullable = false, length = 20)
    private String ticketNumber;

    // ── Phân loại AI (M1–M5) ────────────────────────────────────────────────

    /**
     * M1 = Ngay lập tức (Cấp cứu)
     * M2 = Khẩn cấp (≤ 15 phút)
     * M3 = Ưu tiên (≤ 30 phút)
     * M4 = Thông thường (≤ 60 phút)
     * M5 = Không khẩn (Có thể hẹn)
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Priority priority = Priority.M3;

    /** Độ tin cậy phân luồng của AI (0–100%) */
    @Column(name = "ai_confidence")
    private Integer aiConfidence;

    /** Tóm tắt triệu chứng do AI ghi lại */
    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    // ── Trạng thái ──────────────────────────────────────────────────────────

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.WAITING;

    /** Chỉ dẫn đường đến phòng khám */
    @Column(name = "location_guide", columnDefinition = "TEXT")
    private String locationGuide;

    // ── Chuyển khoa ─────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferred_to_dept_id")
    private Department transferredToDepartment;

    @Column(name = "transfer_reason", columnDefinition = "TEXT")
    private String transferReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "called_by_nurse_id")
    private User calledByNurse;

    // ── Timestamps ──────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = true)
    private LocalDateTime createdAt;

    @Column(name = "called_at")
    private LocalDateTime calledAt;

    @Column(name = "done_at")
    private LocalDateTime doneAt;

    // ── Enums ───────────────────────────────────────────────────────────────

    public enum Priority {
        M1, M2, M3, M4, M5
    }

    public enum Status {
        WAITING,
        CALLED,
        DONE,
        CANCELLED,
        TRANSFERRED
    }

    public String getPatientName() {
        return patient != null ? patient.getFullName() : "";
    }

    public String getBhytNumber() {
        return patient != null ? patient.getBhytNumber() : "";
    }

    public String getDepartmentName() {
        return department != null ? department.getName() : "";
    }

    public String getStatusName() {
        return status != null ? status.name() : "";
    }

    public String getPriorityName() {
        return priority != null ? priority.name() : "";
    }
}

