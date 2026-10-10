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
@Table(name = "AI_ANALYSIS_LOGS", indexes = {
    @Index(name = "idx_ailogs_message", columnList = "message_id")
})
public class AiAnalysisLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private ChatMessage message;

    @Column(name = "ai_model_used", length = 50)
    private String aiModelUsed;

    @Column(name = "input_text", columnDefinition = "TEXT")
    private String inputText;

    @Column(name = "predicted_dept", length = 100)
    private String predictedDept;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(length = 20)
    private String sentiment;

    @Builder.Default
    @Column(name = "is_corrected")
    private Boolean isCorrected = false;

    @Column(name = "corrected_dept", length = 100)
    private String correctedDept;

    @Column(name = "processing_ms")
    private Integer processingMs;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = true)
    private LocalDateTime createdAt;
}

