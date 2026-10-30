package com.ucop.hethongphanluongbenhvien.repository;

import com.ucop.hethongphanluongbenhvien.entity.VisitRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VisitRecordRepository extends JpaRepository<VisitRecord, UUID> {
    List<VisitRecord> findByPatientIdOrderByVisitDateDesc(UUID patientId);
    List<VisitRecord> findByDepartmentIdOrderByVisitDateDesc(UUID departmentId);
    Optional<VisitRecord> findByTicketId(UUID ticketId);
    boolean existsByTicketId(UUID ticketId);
}