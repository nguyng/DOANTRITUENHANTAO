package com.ucop.hethongphanluongbenhvien.repository;

import com.ucop.hethongphanluongbenhvien.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaqRepository extends JpaRepository<Faq, UUID> {
    List<Faq> findByIsActiveTrueOrderBySortOrderAsc();
    List<Faq> findByIsActiveTrueAndShowAsQuickReplyTrueOrderBySortOrderAsc();
    List<Faq> findByDepartmentIdAndIsActiveTrue(UUID departmentId);
}
