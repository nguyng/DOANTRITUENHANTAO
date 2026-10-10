package com.ucop.hethongphanluongbenhvien.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "USERS", indexes = {
    @Index(name = "idx_users_email",    columnList = "email",       unique = true),
    @Index(name = "idx_users_bhyt",     columnList = "bhyt_number", unique = true),
    @Index(name = "idx_users_role",     columnList = "role")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Mã BHYT — cũng là username đăng nhập cho nhân viên */
    @Column(name = "bhyt_number", length = 30, unique = true)
    private String bhytNumber;

    /** Mật khẩu đã hash (BCrypt) */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 15, unique = true)
    private String phone;

    @Column(length = 20, unique = true)
    private String cccd;

    @Column(length = 100)
    private String email;

    /** Ngày sinh — khớp với cột "dob" trong SQL */
    @Column(name = "dob")
    private LocalDate dob;

    @Column(length = 10)
    private String gender;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    // ── Phân quyền ──────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** Khoa phụ trách — áp dụng cho NURSE và DOCTOR */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // ── Thông tin BHYT (Bệnh nhân) ──────────────────────────────────────────

    /** Mã đối tượng BHYT — VD: "HN", "CN", "TE", "HS", "DN" */
    @Column(name = "bhyt_object_code", length = 2)
    private String bhytObjectCode;

    /** Tên đối tượng — VD: "Hộ nghèo", "Người lao động" */
    @Column(name = "bhyt_object_name", length = 50)
    private String bhytObjectName;

    /** Tỷ lệ hưởng BHYT: 0.80, 0.95, 1.00 */
    @Column(name = "bhyt_discount_rate", precision = 3, scale = 2)
    private BigDecimal bhytDiscountRate;

    /** Ngày hết hạn thẻ BHYT */
    @Column(name = "bhyt_expiry_date")
    private LocalDate bhytExpiryDate;

    /** Ngày cấp thẻ BHYT */
    @Column(name = "bhyt_issued_date")
    private LocalDate bhytIssuedDate;

    // ── Quản trị ────────────────────────────────────────────────────────────

    /** Tài khoản được tạo bởi admin nào */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = true)
    private LocalDateTime createdAt;

    // ── Enum ─────────────────────────────────────────────────────────────────

    public enum Role {
        PATIENT,  // Bệnh nhân
        NURSE,    // Y tá / Lễ tân
        DOCTOR,   // Bác sĩ
        ADMIN     // Quản trị viên
    }
}

