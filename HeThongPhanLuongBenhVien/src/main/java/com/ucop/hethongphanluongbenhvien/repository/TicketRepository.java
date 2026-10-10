package com.ucop.hethongphanluongbenhvien.repository;

import com.ucop.hethongphanluongbenhvien.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
    
    List<Ticket> findByDepartmentIdAndStatusOrderByPriorityAscCreatedAtAsc(UUID departmentId, Ticket.Status status);
    
    @Query("SELECT t FROM Ticket t WHERE t.department.id = :departmentId AND t.status IN :statuses " +
           "ORDER BY CASE WHEN t.status = :calledStatus THEN 0 ELSE 1 END, t.priority ASC, t.createdAt ASC")
    List<Ticket> findByDepartmentIdAndStatuses(
            @Param("departmentId") UUID departmentId,
            @Param("statuses") java.util.Collection<Ticket.Status> statuses,
            @Param("calledStatus") Ticket.Status calledStatus);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.department = :department AND t.status = :status AND t.createdAt < :createdAt")
    long countWaitersBefore(@Param("department") com.ucop.hethongphanluongbenhvien.entity.Department department, 
                            @Param("status") Ticket.Status status, 
                            @Param("createdAt") LocalDateTime createdAt);
                            
    long countByDepartmentIdAndStatus(UUID departmentId, Ticket.Status status);
}
