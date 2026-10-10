package com.ucop.hethongphanluongbenhvien.service;

import com.ucop.hethongphanluongbenhvien.entity.Department;
import com.ucop.hethongphanluongbenhvien.entity.Ticket;
import com.ucop.hethongphanluongbenhvien.repository.DepartmentRepository;
import com.ucop.hethongphanluongbenhvien.repository.TicketRepository;
import com.ucop.hethongphanluongbenhvien.repository.UserRepository;
import com.ucop.hethongphanluongbenhvien.entity.User;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final TicketRepository    ticketRepository;
    private final UserRepository      userRepository;
    private final DepartmentRepository departmentRepository;

    // ── Dashboard Stats ──────────────────────────────────────────────────────────

    /**
     * Trả về toàn bộ số liệu cho trang Admin Dashboard.
     * Dùng DTO AdminStats để truyền vào Thymeleaf Model.
     */
    public AdminStats getDashboardStats() {
        long totalPatients = ticketRepository.count();

        long activeStaff = userRepository.findByRole(User.Role.NURSE).stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsActive())).count()
                + userRepository.findByRole(User.Role.DOCTOR).stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsActive())).count();

        long totalStaff = userRepository.findByRole(User.Role.NURSE).size()
                + userRepository.findByRole(User.Role.DOCTOR).size();

        // Đếm khoa đang có bệnh nhân đang chờ
        long activeDepts = departmentRepository.findByIsActiveTrue().stream()
                .filter(d -> ticketRepository.countByDepartmentIdAndStatus(d.getId(), Ticket.Status.WAITING) > 0)
                .count();

        // Số phiếu bị chuyển khoa = proxy cho AI phân luồng sai
        long transferCount = ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() == Ticket.Status.TRANSFERRED)
                .count();

        long doneCount = ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() == Ticket.Status.DONE || t.getStatus() == Ticket.Status.TRANSFERRED)
                .count();

        // Accuracy = (hoàn tất - chuyển khoa) / hoàn tất * 100
        int aiAccuracy = doneCount > 0
                ? (int) Math.round((double)(doneCount - transferCount) / doneCount * 100)
                : 92; // default khi chưa có data thực

        int aiErrorRate = 100 - aiAccuracy;

        long correctTriageCount = doneCount - transferCount;

        return AdminStats.builder()
                .totalPatients((int) totalPatients)
                .activeStaff((int) activeStaff)
                .totalStaff((int) totalStaff)
                .activeDepts((int) activeDepts)
                .totalChats(ticketRepository.count()) // 1 ticket = 1 chat session
                .aiAccuracy(aiAccuracy)
                .transferCount((int) transferCount)
                .aiErrorRate(aiErrorRate)
                .correctTriageCount((int) correctTriageCount)
                .build();
    }

    /** Phân bổ bệnh nhân theo khoa — cho biểu đồ doughnut */
    public Map<String, Long> getPatientsByDepartment() {
        return departmentRepository.findByIsActiveTrue().stream()
                .collect(Collectors.toMap(
                        Department::getName,
                        d -> ticketRepository.countByDepartmentIdAndStatus(d.getId(), Ticket.Status.WAITING)
                           + ticketRepository.countByDepartmentIdAndStatus(d.getId(), Ticket.Status.CALLED)
                ));
    }

    // ── DTO ──────────────────────────────────────────────────────────────────────

    @Data
    @Builder
    public static class AdminStats {
        private int totalPatients;
        private int activeStaff;
        private int totalStaff;
        private int activeDepts;
        private long totalChats;
        private int aiAccuracy;
        private int transferCount;
        private int aiErrorRate;
        private long correctTriageCount;
    }
}
