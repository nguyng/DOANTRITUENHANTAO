package com.ucop.hethongphanluongbenhvien.repository;

import com.ucop.hethongphanluongbenhvien.entity.VisitService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VisitServiceRepository extends JpaRepository<VisitService, UUID> {
    List<VisitService> findByVisitRecordId(UUID visitRecordId);
}
