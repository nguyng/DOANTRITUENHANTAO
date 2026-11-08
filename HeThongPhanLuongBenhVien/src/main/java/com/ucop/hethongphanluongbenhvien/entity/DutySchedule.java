package com.ucop.hethongphanluongbenhvien.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DUTY_SCHEDULES", indexes = {
    @Index(name = "idx_duty_staff",  columnList = "staff_id"),
    @Index(name = "idx_duty_dept",   columnList = "department_id"),
    @Index(name = "idx_duty_date",   columnList = "shift_date")
})
public class DutySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false, length = 30)
    private ShiftType shiftType;

    @Column(name = "room_number", length = 50)
    private String roomNumber;

    @Column(name = "duty_role", length = 50)
    private String dutyRole;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DutyStatus status = DutyStatus.ON_DUTY;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── Enums ────────────────────────────────────────────────────────────────
    public enum ShiftType {
        MORNING("Ca Sáng", "07:00 - 11:30", "bg-sky-50 text-sky-700 border-sky-200"),
        AFTERNOON("Ca Chiều", "13:30 - 17:00", "bg-amber-50 text-amber-700 border-amber-200"),
        NIGHT("Ca Đêm", "17:00 - 07:00", "bg-purple-50 text-purple-700 border-purple-200"),
        FULL_DAY("Trực 24/7", "24/24 Giờ", "bg-rose-50 text-rose-700 border-rose-200");

        private final String label;
        private final String timeRange;
        private final String badgeClass;

        ShiftType(String label, String timeRange, String badgeClass) {
            this.label = label;
            this.timeRange = timeRange;
            this.badgeClass = badgeClass;
        }

        public String getLabel() { return label; }
        public String getTimeRange() { return timeRange; }
        public String getBadgeClass() { return badgeClass; }
    }

    public enum DutyStatus {
        ON_DUTY("Đang trong ca", "bg-emerald-50 text-emerald-700"),
        SCHEDULED("Sắp tới", "bg-slate-50 text-slate-600"),
        COMPLETED("Đã hoàn thành", "bg-gray-100 text-gray-500"),
        OFF("Nghỉ ca", "bg-rose-50 text-rose-600");

        private final String label;
        private final String badgeClass;

        DutyStatus(String label, String badgeClass) {
            this.label = label;
            this.badgeClass = badgeClass;
        }

        public String getLabel() { return label; }
        public String getBadgeClass() { return badgeClass; }
    }

    public String getStaffName() {
        return staff != null ? staff.getFullName() : "—";
    }

    public String getDepartmentName() {
        return department != null ? department.getName() : "—";
    }
}
