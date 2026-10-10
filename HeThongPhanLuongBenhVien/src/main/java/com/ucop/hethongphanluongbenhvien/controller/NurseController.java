package com.ucop.hethongphanluongbenhvien.controller;

import com.ucop.hethongphanluongbenhvien.entity.Department;
import com.ucop.hethongphanluongbenhvien.entity.Ticket;
import com.ucop.hethongphanluongbenhvien.entity.User;
import com.ucop.hethongphanluongbenhvien.service.DepartmentService;
import com.ucop.hethongphanluongbenhvien.service.TicketService;
import com.ucop.hethongphanluongbenhvien.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/nurse")
@RequiredArgsConstructor
public class NurseController {

    private final TicketService     ticketService;
    private final DepartmentService departmentService;
    private final UserService       userService;

    // ── Helper ───────────────────────────────────────────────────────────────────

    private User resolveNurse(UserDetails ud) {
        String u = ud.getUsername();
        return userService.findByBhytNumber(u)
                .or(() -> userService.findByPhone(u))
                .orElseThrow(() -> new RuntimeException("Không xác định được y tá"));
    }

    private void addNurseAttrs(Model model, User nurse) {
        model.addAttribute("userName", nurse.getFullName());
        model.addAttribute("userDept", nurse.getDepartment() != null
                ? nurse.getDepartment().getName() : "Toàn viện");
    }

    // ── Dashboard hàng đợi ────────────────────────────────────────────────────────

    @GetMapping("/queue")
    public String queueDashboard(Model model, @AuthenticationPrincipal UserDetails ud) {
        model.addAttribute("currentPage", "queue");

        User nurse = resolveNurse(ud);
        addNurseAttrs(model, nurse);

        List<Department> depts = departmentService.findActive();

        // Gắn số đếm cho mỗi khoa (dùng DepartmentWrapper DTO)
        List<DeptQueueDto> deptDtos = depts.stream().map(d -> new DeptQueueDto(
                d,
                ticketService.countByDeptAndStatus(d.getId(), Ticket.Status.WAITING),
                ticketService.countByDeptAndStatus(d.getId(), Ticket.Status.CALLED),
                ticketService.countByDeptAndStatus(d.getId(), Ticket.Status.DONE)
        )).toList();

        // Tổng hợp cho stat cards ở đầu trang
        long totalWaiting  = deptDtos.stream().mapToLong(DeptQueueDto::waitingCount).sum();
        long totalCalled   = deptDtos.stream().mapToLong(DeptQueueDto::calledCount).sum();
        long totalDone     = deptDtos.stream().mapToLong(DeptQueueDto::doneCount).sum();
        long totalEmergency = ticketService.findWaitingByDepartment(
                nurse.getDepartment() != null ? nurse.getDepartment().getId() : UUID.randomUUID()
        ).stream().filter(t -> t.getPriority() == Ticket.Priority.M1
                            || t.getPriority() == Ticket.Priority.M2).count();

        model.addAttribute("departments",   deptDtos);
        model.addAttribute("totalWaiting",  totalWaiting);
        model.addAttribute("totalCalled",   totalCalled);
        model.addAttribute("totalDone",     totalDone);
        model.addAttribute("totalEmergency", totalEmergency);
        return "nurse/queue";
    }

    // ── Chi tiết hàng đợi theo khoa ──────────────────────────────────────────────

    @GetMapping("/queue/{deptId}")
    public String queueDetail(@PathVariable UUID deptId, Model model,
                              @AuthenticationPrincipal UserDetails ud) {
        User nurse = resolveNurse(ud);
        addNurseAttrs(model, nurse);

        Department dept = departmentService.findById(deptId);
        List<Ticket> tickets = ticketService.findActiveByDepartment(deptId);

        model.addAttribute("department",  dept);
        model.addAttribute("tickets",     tickets);
        model.addAttribute("waitingCount", ticketService.countByDeptAndStatus(deptId, Ticket.Status.WAITING));
        model.addAttribute("calledCount",  ticketService.countByDeptAndStatus(deptId, Ticket.Status.CALLED));
        model.addAttribute("allDepartments", departmentService.findActive());
        return "nurse/queue-detail";
    }

    // ── Actions ──────────────────────────────────────────────────────────────────

    /** Gọi số tiếp theo */
    @PostMapping("/queue/{deptId}/call-next")
    public String callNext(@PathVariable UUID deptId,
                           @AuthenticationPrincipal UserDetails ud,
                           RedirectAttributes ra) {
        try {
            User nurse = resolveNurse(ud);
            Ticket called = ticketService.callNext(deptId, nurse);
            ra.addFlashAttribute("success", "Đã gọi số " + called.getTicketNumber());
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/nurse/queue/" + deptId;
    }

    /** Gọi một phiếu cụ thể */
    @PostMapping("/queue/{ticketId}/call")
    public String callTicket(@PathVariable UUID ticketId,
                             @RequestParam(required = false) UUID deptId,
                             @AuthenticationPrincipal UserDetails ud,
                             RedirectAttributes ra) {
        UUID redirectDept = deptId;
        try {
            User nurse = resolveNurse(ud);
            Ticket called = ticketService.callTicket(ticketId, nurse);
            if (redirectDept == null && called.getDepartment() != null) {
                redirectDept = called.getDepartment().getId();
            }
            ra.addFlashAttribute("success", "Đã gọi bệnh nhân");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/nurse/queue/" + (redirectDept != null ? redirectDept : "");
    }

    /** Hoàn tất khám */
    @PostMapping("/queue/{ticketId}/done")
    public String markDone(@PathVariable UUID ticketId,
                           @RequestParam(required = false) UUID deptId,
                           RedirectAttributes ra) {
        UUID redirectDept = deptId;
        try {
            Ticket ticket = ticketService.markDone(ticketId);
            if (redirectDept == null && ticket.getDepartment() != null) {
                redirectDept = ticket.getDepartment().getId();
            }
            ra.addFlashAttribute("success", "Hoàn tất khám");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/nurse/queue/" + (redirectDept != null ? redirectDept : "");
    }

    /** Chuyển khoa */
    @PostMapping("/queue/transfer")
    public String transfer(@RequestParam UUID ticketId,
                           @RequestParam UUID newDepartmentId,
                           @RequestParam(required = false) String reason,
                           @RequestParam UUID deptId,
                           @AuthenticationPrincipal UserDetails ud,
                           RedirectAttributes ra) {
        try {
            User nurse = resolveNurse(ud);
            Ticket newTicket = ticketService.transfer(ticketId, newDepartmentId, reason, nurse);
            ra.addFlashAttribute("success", "Đã chuyển khoa — Phiếu mới: " + newTicket.getTicketNumber());
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/nurse/queue/" + deptId;
    }

    /** Tìm kiếm bệnh nhân */
    @GetMapping("/search")
    public String search(Model model, @AuthenticationPrincipal UserDetails ud) {
        model.addAttribute("currentPage", "search");
        User nurse = resolveNurse(ud);
        addNurseAttrs(model, nurse);
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
