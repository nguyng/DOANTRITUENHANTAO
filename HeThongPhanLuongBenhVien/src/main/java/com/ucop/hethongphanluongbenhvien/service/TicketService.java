package com.ucop.hethongphanluongbenhvien.service;

import com.ucop.hethongphanluongbenhvien.entity.Department;
import com.ucop.hethongphanluongbenhvien.entity.Ticket;
import com.ucop.hethongphanluongbenhvien.entity.User;
import com.ucop.hethongphanluongbenhvien.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final DepartmentService departmentService;

    // ── Tạo phiếu khám ─────────────────────────────────────────────────────────

    @Transactional
    public Ticket issueTicket(User patient, UUID departmentId,
                              Ticket.Priority priority, String aiSummary, Integer aiConfidence) {

        Department dept = departmentService.findById(departmentId);

        // Đếm tổng phiếu đã phát hôm nay cho khoa này → dùng làm số thứ tự
        long todayCount = ticketRepository.countByDepartmentIdAndStatus(departmentId, Ticket.Status.WAITING)
                        + ticketRepository.countByDepartmentIdAndStatus(departmentId, Ticket.Status.CALLED)
                        + ticketRepository.countByDepartmentIdAndStatus(departmentId, Ticket.Status.DONE);

        String ticketNumber = String.format("%s-%03d", dept.getTicketPrefix(), todayCount + 1);

        Ticket ticket = Ticket.builder()
                .patient(patient)
                .department(dept)
                .ticketNumber(ticketNumber)
                .priority(priority != null ? priority : Ticket.Priority.M3)
                .aiSummary(aiSummary)
                .aiConfidence(aiConfidence)
                .status(Ticket.Status.WAITING)
                .locationGuide(dept.getLocationGuide())
                .build();

        return ticketRepository.save(ticket);
    }

    // ── Đọc ─────────────────────────────────────────────────────────────────────

    public Ticket findById(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu khám: " + id));
    }

    /** Danh sách đang chờ của một khoa — sắp xếp: M1 lên đầu, cùng mức thì FIFO */
    public List<Ticket> findWaitingByDepartment(UUID departmentId) {
        return ticketRepository
                .findByDepartmentIdAndStatusOrderByPriorityAscCreatedAtAsc(departmentId, Ticket.Status.WAITING);
    }

    /** Danh sách đang khám + chờ khám của một khoa — CALLED lên đầu để xử lý, sau đó tới WAITING */
    public List<Ticket> findActiveByDepartment(UUID departmentId) {
        return ticketRepository.findByDepartmentIdAndStatuses(
                departmentId,
                List.of(Ticket.Status.CALLED, Ticket.Status.WAITING),
                Ticket.Status.CALLED
        );
    }

    public List<Ticket> findByPatient(UUID patientId) {
        return ticketRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public long countByDeptAndStatus(UUID departmentId, Ticket.Status status) {
        return ticketRepository.countByDepartmentIdAndStatus(departmentId, status);
    }

    /**
     * Tính số người chờ trước bệnh nhân và thời gian chờ dự kiến.
     * @return mảng [queuePosition, estimatedWaitMinutes]
     */
    public int[] calculateWaitInfo(Ticket ticket) {
        Department dept = ticket.getDepartment();
        long position = ticketRepository.countWaitersBefore(
                dept, Ticket.Status.WAITING, ticket.getCreatedAt());
        int waitMinutes = (int) position * (dept.getAvgConsultMinutes() != null ? dept.getAvgConsultMinutes() : 15);
        return new int[]{(int) position, waitMinutes};
    }

    // ── Hành động Y tá ──────────────────────────────────────────────────────────

    /** Y tá gọi số tiếp theo của khoa */
    @Transactional
    public Ticket callNext(UUID departmentId, User nurse) {
        List<Ticket> waiting = findWaitingByDepartment(departmentId);
        if (waiting.isEmpty()) {
            throw new RuntimeException("Hàng đợi trống, không có bệnh nhân nào đang chờ");
        }
        Ticket next = waiting.get(0);
        next.setStatus(Ticket.Status.CALLED);
        next.setCalledAt(LocalDateTime.now());
        next.setCalledByNurse(nurse);
        return ticketRepository.save(next);
    }

    /** Y tá gọi một phiếu cụ thể */
    @Transactional
    public Ticket callTicket(UUID ticketId, User nurse) {
        Ticket ticket = findById(ticketId);
        if (ticket.getStatus() != Ticket.Status.WAITING) {
            throw new RuntimeException("Phiếu này không ở trạng thái chờ");
        }
        ticket.setStatus(Ticket.Status.CALLED);
        ticket.setCalledAt(LocalDateTime.now());
        ticket.setCalledByNurse(nurse);
        return ticketRepository.save(ticket);
    }

    /** Hoàn tất khám */
    @Transactional
    public Ticket markDone(UUID ticketId) {
        Ticket ticket = findById(ticketId);
        ticket.setStatus(Ticket.Status.DONE);
        ticket.setDoneAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    /** Chuyển khoa — y tá override quyết định của AI */
    @Transactional
    public Ticket transfer(UUID ticketId, UUID newDepartmentId, String reason, User nurse) {
        Ticket ticket   = findById(ticketId);
        Department newDept = departmentService.findById(newDepartmentId);

        // Lưu lại khoa cũ để audit
        ticket.setStatus(Ticket.Status.TRANSFERRED);
        ticket.setTransferredToDepartment(newDept);
        ticket.setTransferReason(reason);
        ticketRepository.save(ticket);

        // Phát phiếu mới ở khoa mới — kế thừa priority
        return issueTicket(ticket.getPatient(), newDepartmentId,
                ticket.getPriority(), ticket.getAiSummary(), ticket.getAiConfidence());
    }

    /** Bệnh nhân tự hủy phiếu */
    @Transactional
    public Ticket cancel(UUID ticketId, UUID patientId) {
        Ticket ticket = findById(ticketId);
        if (!ticket.getPatient().getId().equals(patientId)) {
            throw new RuntimeException("Bạn không có quyền hủy phiếu này");
        }
        if (ticket.getStatus() != Ticket.Status.WAITING) {
            throw new RuntimeException("Chỉ hủy được phiếu đang ở trạng thái chờ");
        }
        ticket.setStatus(Ticket.Status.CANCELLED);
        return ticketRepository.save(ticket);
    }
}
