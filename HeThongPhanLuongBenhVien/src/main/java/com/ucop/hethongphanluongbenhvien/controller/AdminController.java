package com.ucop.hethongphanluongbenhvien.controller;

import com.ucop.hethongphanluongbenhvien.entity.Department;
import com.ucop.hethongphanluongbenhvien.entity.Faq;
import com.ucop.hethongphanluongbenhvien.entity.Ticket;
import com.ucop.hethongphanluongbenhvien.entity.User;
import com.ucop.hethongphanluongbenhvien.repository.FaqRepository;
import com.ucop.hethongphanluongbenhvien.repository.UserRepository;
import com.ucop.hethongphanluongbenhvien.repository.VisitRecordRepository;
import com.ucop.hethongphanluongbenhvien.repository.AiAnalysisLogRepository;
import com.ucop.hethongphanluongbenhvien.service.AdminService;
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

import java.util.UUID;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService      adminService;
    private final UserService       userService;
    private final DepartmentService departmentService;
    private final TicketService     ticketService;
    private final FaqRepository     faqRepository;
    private final VisitRecordRepository visitRecordRepository;
    private final AiAnalysisLogRepository aiAnalysisLogRepository;

    // ── Helper ───────────────────────────────────────────────────────────────────

    private void addAdminAttrs(Model model, UserDetails ud) {
        if (ud == null) { model.addAttribute("userName", "Admin"); return; }
        String u = ud.getUsername();
        String name = userService.findByPhone(u)
                .or(() -> userService.findByBhytNumber(u))
                .map(User::getFullName)
                .orElse("Admin");
        model.addAttribute("userName", name);
    }

    // ── Dashboard ────────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails ud) {
        model.addAttribute("currentPage", "dashboard");
        addAdminAttrs(model, ud);

        // Lấy dữ liệu thật từ DB thông qua AdminService
        AdminService.AdminStats stats = adminService.getDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("deptDistribution", adminService.getPatientsByDepartment());
        return "admin/dashboard";
    }

    // ── Shortcuts (URLs phổ biến) ─────────────────────────────────────────────

    @GetMapping("/users")
    public String users() { return "redirect:/admin/staff"; }

    @GetMapping("/patients")
    public String patients(Model model, @AuthenticationPrincipal UserDetails ud) {
        model.addAttribute("currentPage", "patients");
        addAdminAttrs(model, ud);
        model.addAttribute("patientList", userService.findAllByRole(User.Role.PATIENT));
        model.addAttribute("departments",  departmentService.findActive());
        return "admin/patients";
    }

    // ── Quản lý nhân viên ────────────────────────────────────────────────────────

    @GetMapping("/staff")
    public String staffList(Model model, @AuthenticationPrincipal UserDetails ud) {
        model.addAttribute("currentPage", "staff");
        addAdminAttrs(model, ud);
        model.addAttribute("staffList",    userService.findAllByRole(User.Role.NURSE));
        model.addAttribute("doctorList",   userService.findAllByRole(User.Role.DOCTOR));
        model.addAttribute("departments",  departmentService.findActive());
        return "admin/staff";
    }

    @PostMapping("/staff/create")
    public String createStaff(@ModelAttribute User staff, RedirectAttributes ra) {
        try {
            userService.createStaff(staff);
            ra.addFlashAttribute("success", "Tạo nhân viên thành công: " + staff.getFullName());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/staff";
    }

    @PostMapping("/staff/{id}/toggle")
    public String toggleStaff(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            userService.toggleActive(id);
            ra.addFlashAttribute("success", "Đã cập nhật trạng thái nhân viên");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/staff";
    }

    // ── Quản lý FAQ ───────────────────────────────────────────────────────────────

    @GetMapping("/faq")
    public String faqList(Model model, @AuthenticationPrincipal UserDetails ud) {
        model.addAttribute("currentPage", "faq");
        addAdminAttrs(model, ud);
        model.addAttribute("faqList",    faqRepository.findByIsActiveTrueOrderBySortOrderAsc());
        model.addAttribute("departments", departmentService.findAll());
        return "admin/faq";
    }

    @PostMapping("/faq/create")
    public String createFaq(@ModelAttribute Faq faq,
                            @AuthenticationPrincipal UserDetails ud,
                            RedirectAttributes ra) {
        try {
            userService.findByPhone(ud.getUsername()).ifPresent(faq::setCreatedBy);
            faqRepository.save(faq);
            ra.addFlashAttribute("success", "Đã thêm câu hỏi mới");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/faq";
    }

    @PostMapping("/faq/{id}/delete")
    public String deleteFaq(@PathVariable UUID id, RedirectAttributes ra) {
        faqRepository.deleteById(id);
        ra.addFlashAttribute("success", "Đã xóa câu hỏi");
        return "redirect:/admin/faq";
    }

    // ── Báo cáo ─────────────────────────────────────────────────────────────────

    @GetMapping("/reports")
    public String reports(Model model, @AuthenticationPrincipal UserDetails ud) {
        model.addAttribute("currentPage", "reports");
        addAdminAttrs(model, ud);

        AdminService.AdminStats stats = adminService.getDashboardStats();
        long totalVisits = visitRecordRepository.count();

        model.addAttribute("report", java.util.Map.of(
                "totalVisits",    totalVisits,
                "aiAccuracy",     stats.getAiAccuracy() + "%",
                "transferCount",  stats.getTransferCount(),
                "totalRevenue",   "125,500,000đ", // TODO: Lấy từ DB
                "totalInsurance", "98,200,000đ"   // TODO: Lấy từ DB
        ));
        model.addAttribute("stats",           stats);
        model.addAttribute("topDepartments",  adminService.getPatientsByDepartment());
        return "admin/reports";
    }

    // ── Quản lý Khoa ──────────────────────────────────────────────────────────────

    @GetMapping("/departments")
    public String departmentList(Model model, @AuthenticationPrincipal UserDetails ud) {
        model.addAttribute("currentPage", "departments");
        addAdminAttrs(model, ud);
        model.addAttribute("departments", departmentService.findAll());
        return "admin/departments";
    }

    @PostMapping("/departments/create")
    public String createDepartment(@ModelAttribute Department dept, RedirectAttributes ra) {
        try {
            departmentService.save(dept);
            ra.addFlashAttribute("success", "Đã thêm khoa: " + dept.getName());
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/departments";
    }

    // ── Logs hệ thống / Nhật ký phân luồng AI ─────────────────────────────────────

    @GetMapping("/logs")
    public String systemLogs(Model model, @AuthenticationPrincipal UserDetails ud) {
        model.addAttribute("currentPage", "logs");
        addAdminAttrs(model, ud);
        model.addAttribute("logs", aiAnalysisLogRepository.findAllByOrderByCreatedAtDesc());
        return "admin/logs";
    }
}