package com.ucop.hethongphanluongbenhvien.repository;

import com.ucop.hethongphanluongbenhvien.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    List<ChatSession> findByUserIdOrderByStartedAtDesc(UUID userId);
}
