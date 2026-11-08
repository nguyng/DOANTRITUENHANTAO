package com.ucop.hethongphanluongbenhvien.service;

import com.ucop.hethongphanluongbenhvien.entity.Department;
import com.ucop.hethongphanluongbenhvien.entity.DutySchedule;
import com.ucop.hethongphanluongbenhvien.entity.User;
import com.ucop.hethongphanluongbenhvien.repository.DepartmentRepository;
import com.ucop.hethongphanluongbenhvien.repository.DutyScheduleRepository;
import com.ucop.hethongphanluongbenhvien.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DutyScheduleService {

    private final DutyScheduleRepository dutyScheduleRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    /** Lấy ca trực hôm nay của một nhân viên (Bác sĩ hoặc Y tá) */
    public Optional<DutySchedule> findTodayShift(UUID staffId) {
        List<DutySchedule> shifts = dutyScheduleRepository.findByStaffIdAndShiftDate(staffId, LocalDate.now());
        return shifts.isEmpty() ? Optional.empty() : Optional.of(shifts.get(0));
    }

    /** Lấy toàn bộ nhân viên đang trực hôm nay tại một chuyên khoa */
    public List<DutySchedule> findTodayShiftsByDepartment(UUID departmentId) {
        return dutyScheduleRepository.findByDepartmentIdAndShiftDate(departmentId, LocalDate.now());
    }

    /** Lấy lịch trực cả tuần của một chuyên khoa */
    public List<DutySchedule> findWeeklyScheduleByDepartment(UUID departmentId, LocalDate targetDate) {
        LocalDate date = targetDate != null ? targetDate : LocalDate.now();
        LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return dutyScheduleRepository.findByDepartmentIdAndShiftDateBetweenOrderByShiftDateAsc(departmentId, monday, sunday);
    }

    /** Lấy toàn bộ lịch trực toàn viện hôm nay */
    public List<DutySchedule> findAllToday() {
        return dutyScheduleRepository.findByShiftDateOrderByDepartmentAsc(LocalDate.now());
    }

    /** Lấy danh sách ca trực của một nhân viên */
    public List<DutySchedule> findStaffSchedule(UUID staffId) {
        return dutyScheduleRepository.findByStaffIdOrderByShiftDateDesc(staffId);
    }

    /** Tạo hoặc phân công ca trực mới */
    @Transactional
    public DutySchedule scheduleShift(UUID staffId, UUID departmentId, LocalDate date,
                                      DutySchedule.ShiftType shiftType, String roomNumber,
                                      String dutyRole, DutySchedule.DutyStatus status, String notes) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên: " + staffId));
        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa: " + departmentId));

        DutySchedule schedule = DutySchedule.builder()
                .staff(staff)
                .department(dept)
                .shiftDate(date)
                .shiftType(shiftType)
                .roomNumber(roomNumber)
                .dutyRole(dutyRole)
                .status(status != null ? status : DutySchedule.DutyStatus.ON_DUTY)
                .notes(notes)
                .build();

        return dutyScheduleRepository.save(schedule);
    }
}
