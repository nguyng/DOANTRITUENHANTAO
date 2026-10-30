package com.ucop.hethongphanluongbenhvien.repository;

import com.ucop.hethongphanluongbenhvien.entity.AiAnalysisLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiAnalysisLogRepository extends JpaRepository<AiAnalysisLog, UUID> {
    Optional<AiAnalysisLog> findByMessageId(UUID messageId);
    List<AiAnalysisLog> findAllByOrderByCreatedAtDesc();
}