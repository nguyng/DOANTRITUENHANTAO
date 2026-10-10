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
@Table(name = "MEDICAL_SERVICES")
public class MedicalService {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Builder.Default
    @Column(name = "is_bhyt_covered")
    private Boolean isBhytCovered = true;

    @Column(name = "bhyt_price_limit", precision = 12, scale = 2)
    private BigDecimal bhytPriceLimit;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = true)
    private LocalDateTime createdAt;
}

