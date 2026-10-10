package com.ucop.hethongphanluongbenhvien.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "VISIT_RECORDS", indexes = {
    @Index(name = "idx_visit_patient",  columnList = "patient_id"),
    @Index(name = "idx_visit_dept",     columnList = "department_id"),
    @Index(name = "idx_visit_ticket",   columnList = "ticket_id"),
    @Index(name = "idx_visit_created",  columnList = "created_at")
})
public class VisitRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ── Quan hệ ───────────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private User doctor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    // ── Thông tin khám ────────────────────────────────────────────────────────

    @Column(name = "visit_name", nullable = false, length = 200)
    private String visitName;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "visit_date")
    private LocalDate visitDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status = Status.IN_PROGRESS;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // ── Thanh toán ────────────────────────────────────────────────────────────

    @Builder.Default
    @Column(name = "original_cost", precision = 12, scale = 2)
    private BigDecimal originalCost = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "insurance_paid", precision = 12, scale = 2)
    private BigDecimal insurancePaid = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "final_cost", precision = 12, scale = 2)
    private BigDecimal finalCost = BigDecimal.ZERO;

    // ── Timestamps ────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = true)
    private LocalDateTime createdAt;

    // ── Enum ─────────────────────────────────────────────────────────────────

    public enum Status {
        IN_PROGRESS, // Đang khám
        COMPLETED,   // Hoàn tất
        CANCELLED    // Đã hủy
    }
}

