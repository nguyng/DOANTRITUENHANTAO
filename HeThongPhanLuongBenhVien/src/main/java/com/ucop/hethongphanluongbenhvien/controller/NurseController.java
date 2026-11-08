package com.ucop.hethongphanluongbenhvien.controller;

import com.ucop.hethongphanluongbenhvien.entity.Department;
import com.ucop.hethongphanluongbenhvien.entity.DutySchedule;
import com.ucop.hethongphanluongbenhvien.entity.Ticket;
import com.ucop.hethongphanluongbenhvien.entity.User;
import com.ucop.hethongphanluongbenhvien.entity.VisitRecord;
import com.ucop.hethongphanluongbenhvien.repository.VisitRecordRepository;
import com.ucop.hethongphanluongbenhvien.service.DepartmentService;
import com.ucop.hethongphanluongbenhvien.service.DutyScheduleService;
import com.ucop.hethongphanluongbenhvien.service.TicketService;
import com.ucop.hethongphanluongbenhvien.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/nurse")
@RequiredArgsConstructor
public class NurseController {

    private final TicketService         ticketService;
    private final DepartmentService     departmentService;
    private final UserService           userService;
    private final DutyScheduleService   dutyScheduleService;
    private final VisitRecordRepository visitRecordRepository;

    // ── Helper ───────────────────────────────────────────────────────────────────

    private User resolveStaff(UserDetails ud) {
        String u = ud.getUsername();
        return userService.findByAnyIdentifier(u)
                .or(() -> userService.findByBhytNumber(u))
                .or(() -> userService.findByPhone(u))
                .orElseThrow(() -> new RuntimeException("Không xác định được nhân viên y tế: " + u));
    }

    private void addStaffAttrs(Model model, User staff) {
        model.addAttribute("staffId", staff.getId());
        model.addAttribute("userName", staff.getFullName());
        model.addAttribute("userRole", staff.getRole().name());
        
        String roleLabel = "Y tá / Điều dưỡng";
        if (staff.getRole() == User.Role.DOCTOR) {
            roleLabel = "Bác sĩ chuyên khoa";
        } else if (staff.getRole() == User.Role.ADMIN) {
            roleLabel = "Quản trị viên";
        }
        model.addAttribute("userRoleLabel", roleLabel);
        
        model.addAttribute("userDept", staff.getDepartment() != null
                ? staff.getDepartment().getName() : "Toàn viện");
        model.addAttribute("userDeptId", staff.getDepartment() != null
                ? staff.getDepartment().getId() : null);

        // Ca trực hôm nay của nhân viên
        Optional<DutySchedule> todayShift = dutyScheduleService.findTodayShift(staff.getId());
        model.addAttribute("currentShift", todayShift.orElse(null));
    }

    // ── Dashboard hàng đợi ────────────────────────────────────────────────────────

    @GetMapping("/queue")
    public String queueDashboard(Model model, @AuthenticationPrincipal UserDetails ud) {
        User staff = resolveStaff(ud);

        // Bác sĩ và Y tá làm việc theo khoa của mình -> Chuyển thẳng về khoa phụ trách
        if (staff.getDepartment() != null && staff.getRole() != User.Role.ADMIN) {
            return "redirect:/nurse/queue/" + staff.getDepartment().getId();
        }

        // Dành riêng cho ADMIN (hoặc nhân viên toàn viện) xem toàn cảnh bệnh viện
        model.addAttribute("currentPage", "queue");
        addStaffAttrs(model, staff);

        List<Department> depts = departmentService.findActive();

        List<DeptQueueDto> deptDtos = depts.stream().map(d -> new DeptQueueDto(
                d,
                ticketService.countByDeptAndStatus(d.getId(), Ticket.Status.WAITING),
                ticketService.countByDeptAndStatus(d.getId(), Ticket.Status.CALLED),
                ticketService.countByDeptAndStatus(d.getId(), Ticket.Status.DONE)
        )).toList();

        long totalWaiting  = deptDtos.stream().mapToLong(DeptQueueDto::waitingCount).sum();
        long totalCalled   = deptDtos.stream().mapToLong(DeptQueueDto::calledCount).sum();
        long totalDone     = deptDtos.stream().mapToLong(DeptQueueDto::doneCount).sum();
        long totalEmergency = ticketService.findWaitingByDepartment(
                staff.getDepartment() != null ? staff.getDepartment().getId() : UUID.randomUUID()
        ).stream().filter(t -> t.getPriority() == Ticket.Priority.M1
                            || t.getPriority() == Ticket.Priority.M2).count();

        model.addAttribute("departments",   deptDtos);
        model.addAttribute("totalWaiting",  totalWaiting);
        model.addAttribute("totalCalled",   totalCalled);
        model.addAttribute("totalDone",     totalDone);
        model.addAttribute("totalEmergency", totalEmergency);
        return "nurse/queue";
    }

    // ── Chi tiết hàng đợi theo khoa (Cô lập dữ liệu theo khoa) ────────────────────

    @GetMapping("/queue/{deptId:[0-9a-fA-F\\-]{36}}")
    public String queueDetail(@PathVariable UUID deptId, Model model,
                              @AuthenticationPrincipal UserDetails ud,
                              RedirectAttributes ra) {
        User staff = resolveStaff(ud);
        addStaffAttrs(model, staff);

        // CÔ LẬP DỮ LIỆU KHOA: Bác sĩ / Y tá không được xem khoa khác
        if (staff.getDepartment() != null && staff.getRole() != User.Role.ADMIN
                && !staff.getDepartment().getId().equals(deptId)) {
            Department targetDept = departmentService.findById(deptId);
            String targetName = targetDept != null ? targetDept.getName() : "khoa khác";
            ra.addFlashAttribute("error", "Quyền truy cập bị từ chối: Bạn thuộc Khoa "
                    + staff.getDepartment().getName() + ", không thể xem bệnh nhân của " + targetName + "!");
            return "redirect:/nurse/queue/" + staff.getDepartment().getId();
        }

        Department dept = departmentService.findById(deptId);
        List<Ticket> tickets = ticketService.findActiveByDepartment(deptId);

        // Danh sách ca trực hôm nay tại khoa
        List<DutySchedule> todayShifts = dutyScheduleService.findTodayShiftsByDepartment(deptId);

        model.addAttribute("department",  dept);
        model.addAttribute("tickets",     tickets);
        model.addAttribute("waitingCount", ticketService.countByDeptAndStatus(deptId, Ticket.Status.WAITING));
        model.addAttribute("calledCount",  ticketService.countByDeptAndStatus(deptId, Ticket.Status.CALLED));
        model.addAttribute("todayShifts",  todayShifts);
        model.addAttribute("allDepartments", departmentService.findActive());
        model.addAttribute("currentPage", "queue");
        return "nurse/queue-detail";
    }

    // ── Actions ──────────────────────────────────────────────────────────────────

    /** Gọi số tiếp theo */
    @PostMapping("/queue/{deptId:[0-9a-fA-F\\-]{36}}/call-next")
    public String callNext(@PathVariable UUID deptId,
                           @AuthenticationPrincipal UserDetails ud,
                           RedirectAttributes ra) {
        User staff = resolveStaff(ud);

        // CÔ LẬP DỮ LIỆU: Chỉ được gọi bệnh nhân trong khoa của mình
        if (staff.getDepartment() != null && staff.getRole() != User.Role.ADMIN
                && !staff.getDepartment().getId().equals(deptId)) {
            ra.addFlashAttribute("error", "Bạn không có quyền gọi bệnh nhân của khoa khác!");
            return "redirect:/nurse/queue/" + staff.getDepartment().getId();
        }

        try {
            Ticket called = ticketService.callNext(deptId, staff);
            ra.addFlashAttribute("success", "Đã gọi số " + called.getTicketNumber() + " (" + called.getPatientName() + ")");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/nurse/queue/" + deptId;
    }

    /** Gọi một phiếu cụ thể */
    @PostMapping("/queue/{ticketId:[0-9a-fA-F\\-]{36}}/call")
    public String callTicket(@PathVariable UUID ticketId,
                             @RequestParam(required = false) UUID deptId,
                             @AuthenticationPrincipal UserDetails ud,
                             RedirectAttributes ra) {
        User staff = resolveStaff(ud);
        Ticket ticket = ticketService.findById(ticketId);

        // CÔ LẬP DỮ LIỆU: Kiểm tra phiếu có thuộc khoa nhân viên không
        if (staff.getDepartment() != null && staff.getRole() != User.Role.ADMIN
                && ticket.getDepartment() != null
                && !staff.getDepartment().getId().equals(ticket.getDepartment().getId())) {
            ra.addFlashAttribute("error", "Bạn không có quyền gọi bệnh nhân thuộc khoa khác!");
            return "redirect:/nurse/queue/" + staff.getDepartment().getId();
        }

        UUID redirectDept = deptId != null ? deptId : (ticket.getDepartment() != null ? ticket.getDepartment().getId() : null);
        try {
            Ticket called = ticketService.callTicket(ticketId, staff);
            ra.addFlashAttribute("success", "Đã gọi bệnh nhân " + called.getPatientName() + " (Số " + called.getTicketNumber() + ")");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/nurse/queue/" + (redirectDept != null ? redirectDept : (staff.getDepartment() != null ? staff.getDepartment().getId() : ""));
    }

    /** Hoàn tất khám */
    @PostMapping("/queue/{ticketId:[0-9a-fA-F\\-]{36}}/done")
    public String markDone(@PathVariable UUID ticketId,
                           @RequestParam(required = false) UUID deptId,
                           @AuthenticationPrincipal UserDetails ud,
                           RedirectAttributes ra) {
        User staff = resolveStaff(ud);
        Ticket ticket = ticketService.findById(ticketId);

        // CÔ LẬP DỮ LIỆU: Chỉ hoàn tất phiếu thuộc khoa mình
        if (staff.getDepartment() != null && staff.getRole() != User.Role.ADMIN
                && ticket.getDepartment() != null
                && !staff.getDepartment().getId().equals(ticket.getDepartment().getId())) {
            ra.addFlashAttribute("error", "Bạn không có quyền kết thúc khám cho bệnh nhân của khoa khác!");
            return "redirect:/nurse/queue/" + staff.getDepartment().getId();
        }

        UUID redirectDept = deptId != null ? deptId : (ticket.getDepartment() != null ? ticket.getDepartment().getId() : null);
        try {
            Ticket doneTicket = ticketService.markDone(ticketId);
            ra.addFlashAttribute("success", "Đã hoàn tất khám bệnh nhân " + doneTicket.getPatientName() + " (Số " + doneTicket.getTicketNumber() + ")");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/nurse/queue/" + (redirectDept != null ? redirectDept : (staff.getDepartment() != null ? staff.getDepartment().getId() : ""));
    }

    /** GET fallback an toàn cho /nurse/queue/transfer — tránh lỗi 405 hoặc 400 Bad Request */
    @GetMapping("/queue/transfer")
    public String transferGet(@AuthenticationPrincipal UserDetails ud) {
        User staff = resolveStaff(ud);
        if (staff.getDepartment() != null && staff.getRole() != User.Role.ADMIN) {
            return "redirect:/nurse/queue/" + staff.getDepartment().getId();
        }
        return "redirect:/nurse/queue";
    }

    /** Chuyển khoa */
    @PostMapping("/queue/transfer")
    public String transfer(@RequestParam(required = false) UUID ticketId,
                           @RequestParam(required = false) UUID newDepartmentId,
                           @RequestParam(required = false) String reason,
                           @RequestParam(required = false) UUID deptId,
                           @AuthenticationPrincipal UserDetails ud,
                           RedirectAttributes ra) {
        User staff = resolveStaff(ud);

        if (ticketId == null || newDepartmentId == null) {
            ra.addFlashAttribute("error", "Vui lòng chọn bệnh nhân và chuyên khoa cần chuyển!");
            return "redirect:/nurse/queue" + (deptId != null ? "/" + deptId : (staff.getDepartment() != null ? "/" + staff.getDepartment().getId() : ""));
        }

        Ticket oldTicket = ticketService.findById(ticketId);
        // Kiểm tra quyền: bệnh nhân đang nằm ở khoa của nhân viên thì mới được chuyển
        if (staff.getDepartment() != null && staff.getRole() != User.Role.ADMIN
                && oldTicket.getDepartment() != null
                && !staff.getDepartment().getId().equals(oldTicket.getDepartment().getId())) {
            ra.addFlashAttribute("error", "Bạn không có quyền chuyển bệnh nhân của khoa khác!");
            return "redirect:/nurse/queue/" + staff.getDepartment().getId();
        }

        try {
            Ticket newTicket = ticketService.transfer(ticketId, newDepartmentId, reason, staff);
            ra.addFlashAttribute("success", "Đã chuyển bệnh nhân sang " + newTicket.getDepartment().getName()
                    + " — Số phiếu mới: " + newTicket.getTicketNumber());
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/nurse/queue/" + (staff.getDepartment() != null ? staff.getDepartment().getId() : (deptId != null ? deptId : ""));
    }

    /** In phiếu khám — Y tá in thay bệnh nhân tại quầy */
    @GetMapping("/queue/{ticketId:[0-9a-fA-F\\-]{36}}/print")
    public String printTicket(@PathVariable UUID ticketId, Model model,
                              @AuthenticationPrincipal UserDetails ud) {
        User staff = resolveStaff(ud);
        addStaffAttrs(model, staff);
        Ticket ticket = ticketService.findById(ticketId);
        model.addAttribute("ticket", ticket);
        return "patient/ticket-print";
    }

    // ── Lịch trực & Ca làm việc (Ca làm việc và trực rõ ràng) ────────────────────

    @GetMapping("/schedule")
    public String schedule(Model model, @AuthenticationPrincipal UserDetails ud) {
        model.addAttribute("currentPage", "schedule");
        User staff = resolveStaff(ud);
        addStaffAttrs(model, staff);

        // 1. Ca trực hôm nay của bản thân
        Optional<DutySchedule> myTodayShift = dutyScheduleService.findTodayShift(staff.getId());
        model.addAttribute("myTodayShift", myTodayShift.orElse(null));

        // 2. Toàn bộ lịch trực cá nhân
        List<DutySchedule> mySchedules = dutyScheduleService.findStaffSchedule(staff.getId());
        model.addAttribute("mySchedules", mySchedules);

        // 3. Lịch trực trong tuần của Khoa
        List<DutySchedule> weeklySchedule = staff.getDepartment() != null
                ? dutyScheduleService.findWeeklyScheduleByDepartment(staff.getDepartment().getId(), LocalDate.now())
                : Collections.emptyList();
        model.addAttribute("weeklySchedule", weeklySchedule);

        // 4. Nếu là Admin, có thể xem thêm tất cả các ca trực hôm nay toàn viện
        if (staff.getRole() == User.Role.ADMIN) {
            model.addAttribute("allHospitalTodayShifts", dutyScheduleService.findAllToday());
        }

        return "nurse/schedule";
    }

    // ── Tìm kiếm bệnh nhân theo khoa ─────────────────────────────────────────────

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q,
                         Model model,
                         @AuthenticationPrincipal UserDetails ud) {
        model.addAttribute("currentPage", "search");
        User staff = resolveStaff(ud);
        addStaffAttrs(model, staff);

        if (q != null && !q.isBlank()) {
            model.addAttribute("query", q.trim());
            Optional<User> patientOpt = userService.findByAnyIdentifier(q.trim());
            if (patientOpt.isPresent()) {
                User patient = patientOpt.get();
                model.addAttribute("patient", patient);

                // Lịch sử khám
                List<VisitRecord> allVisits = visitRecordRepository.findByPatientIdOrderByVisitDateDesc(patient.getId());

                // Nếu là bác sĩ / y tá của khoa: đánh dấu rõ những lần khám tại khoa mình
                model.addAttribute("visits", allVisits);
            }
        }

        return "nurse/search";
    }

    // ── DTO nội bộ ───────────────────────────────────────────────────────────────

    public record DeptQueueDto(Department department, long waitingCount, long calledCount, long doneCount) {
        public UUID getId()          { return department.getId(); }
        public String getName()      { return department.getName(); }
        public String getBuilding()  { return department.getBuilding(); }
        public Integer getFloor()    { return department.getFloor(); }
    }
}
