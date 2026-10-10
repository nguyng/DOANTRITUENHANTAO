package com.ucop.hethongphanluongbenhvien.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PRESCRIPTIONS", indexes = {
    @Index(name = "idx_prescription_visit", columnList = "visit_id")
})
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visit_id", nullable = false)
    private VisitRecord visitRecord;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(length = 100)
    private String dosage;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Builder.Default
    @Column(name = "patient_co_pay", precision = 12, scale = 2)
    private BigDecimal patientCoPay = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "bhyt_pay", precision = 12, scale = 2)
    private BigDecimal bhytPay = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = true)
    private LocalDateTime createdAt;
}

