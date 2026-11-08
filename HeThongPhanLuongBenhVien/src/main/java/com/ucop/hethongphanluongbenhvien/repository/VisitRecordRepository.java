package com.ucop.hethongphanluongbenhvien.repository;

import com.ucop.hethongphanluongbenhvien.entity.VisitRecord;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VisitRecordRepository extends JpaRepository<VisitRecord, UUID> {
    @EntityGraph(attributePaths = {"department", "doctor", "patient"})
    List<VisitRecord> findByPatientIdOrderByVisitDateDesc(UUID patientId);

    @EntityGraph(attributePaths = {"department", "doctor", "patient"})
    List<VisitRecord> findByDepartmentIdOrderByVisitDateDesc(UUID departmentId);

    @Override
    @EntityGraph(attributePaths = {"department", "doctor", "patient"})
    Optional<VisitRecord> findById(UUID id);

    Optional<VisitRecord> findByTicketId(UUID ticketId);
    boolean existsByTicketId(UUID ticketId);
}
