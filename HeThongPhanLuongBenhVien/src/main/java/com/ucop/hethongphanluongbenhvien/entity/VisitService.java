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
@Table(name = "VISIT_SERVICES", indexes = {
    @Index(name = "idx_visitservice_visit", columnList = "visit_id")
})
public class VisitService {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visit_id", nullable = false)
    private VisitRecord visitRecord;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private MedicalService medicalService;

    @Builder.Default
    @Column(name = "patient_co_pay", precision = 12, scale = 2)
    private BigDecimal patientCoPay = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "bhyt_pay", precision = 12, scale = 2)
    private BigDecimal bhytPay = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = true)
    private LocalDateTime createdAt;
}

