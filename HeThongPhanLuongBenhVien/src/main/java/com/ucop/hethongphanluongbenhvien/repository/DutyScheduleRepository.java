package com.ucop.hethongphanluongbenhvien.repository;

import com.ucop.hethongphanluongbenhvien.entity.DutySchedule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DutyScheduleRepository extends JpaRepository<DutySchedule, UUID> {

    @EntityGraph(attributePaths = {"staff", "department"})
    List<DutySchedule> findByDepartmentIdAndShiftDate(UUID departmentId, LocalDate shiftDate);

    @EntityGraph(attributePaths = {"staff", "department"})
    List<DutySchedule> findByDepartmentIdAndShiftDateBetweenOrderByShiftDateAsc(UUID departmentId, LocalDate startDate, LocalDate endDate);

    @EntityGraph(attributePaths = {"staff", "department"})
    List<DutySchedule> findByStaffIdAndShiftDate(UUID staffId, LocalDate shiftDate);

    @EntityGraph(attributePaths = {"staff", "department"})
    List<DutySchedule> findByStaffIdOrderByShiftDateDesc(UUID staffId);

    @EntityGraph(attributePaths = {"staff", "department"})
    List<DutySchedule> findByShiftDateOrderByDepartmentAsc(LocalDate shiftDate);
}
