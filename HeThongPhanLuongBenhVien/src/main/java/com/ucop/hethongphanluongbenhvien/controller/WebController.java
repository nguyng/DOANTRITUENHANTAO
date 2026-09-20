package com.ucop.hethongphanluongbenhvien.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class WebController {

    // --- Auth ---
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "auth/change-password";
    }

    // --- Patient ---
    @GetMapping("/chat")
    public String chatPage(Model model) {
        model.addAttribute("currentPage", "chat");
        model.addAttribute("userName", "Nguyễn Thị Lan");
        model.addAttribute("messages", Collections.emptyList());
        return "patient/chat";
    }

    @GetMapping("/ticket")
    public String ticketPage(Model model) {
        model.addAttribute("currentPage", "ticket");
        model.addAttribute("userName", "Nguyễn Thị Lan");
        // Để test giao diện có phiếu khám, bỏ comment đoạn dưới:
        // model.addAttribute("ticket", Map.of("ticketNumber", "007", "departmentName", "Khoa Tim Mạch", "status", "WAITING", "severity", "NORMAL", "createdAt", java.time.LocalDateTime.now(), "locationGuide", "Tầng 3 Tòa B"));
        return "patient/ticket";
    }

    @GetMapping("/profile")
    public String profilePage(Model model) {
        model.addAttribute("currentPage", "profile");
        model.addAttribute("userName", "Nguyễn Thị Lan");
        model.addAttribute("user", Map.of(
            "fullName", "Nguyễn Thị Lan",
            "gender", "Nữ",
            "bhytNumber", "DN47900000001",
            "bhytObjectName", "Người lao động",
            "bhytDiscountRate", 0.8
        ));
        model.addAttribute("bhytExpiringSoon", false);
        model.addAttribute("bhytExpired", false);
        return "patient/profile";
    }

    @GetMapping("/visits")
    public String visitsPage(Model model) {
        model.addAttribute("currentPage", "visits");
        model.addAttribute("userName", "Nguyễn Thị Lan");
        model.addAttribute("visits", Collections.emptyList());
        return "patient/visits";
    }

    @GetMapping("/directions")
    public String directionsPage(Model model) {
        model.addAttribute("currentPage", "directions");
        model.addAttribute("userName", "Nguyễn Thị Lan");
        model.addAttribute("departments", Collections.emptyList());
        return "patient/directions";
    }

    // --- Nurse ---
    @GetMapping("/nurse/queue")
    public String nurseQueue(Model model) {
        model.addAttribute("currentPage", "queue");
        model.addAttribute("userName", "Y tá Nga");
        model.addAttribute("userDept", "Khoa Cấp Cứu");
        
        model.addAttribute("totalWaiting", 24);
        model.addAttribute("totalCalled", 5);
        model.addAttribute("totalDone", 18);
        model.addAttribute("totalEmergency", 2);
        model.addAttribute("departments", Collections.emptyList());
        return "nurse/queue";
    }

    @GetMapping("/nurse/search")
    public String nurseSearch(Model model) {
        model.addAttribute("currentPage", "search");
        model.addAttribute("userName", "Y tá Nga");
        model.addAttribute("userDept", "Khoa Cấp Cứu");
        return "nurse/search";
    }

    // --- Admin ---
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("currentPage", "dashboard");
        model.addAttribute("userName", "Super Admin");
        
        // Mock data for stats
        model.addAttribute("stats", Map.of(
            "totalPatients", 47,
            "activeStaff", 23,
            "activeDepts", 11,
            "totalChats", 128
        ));
        return "admin/dashboard";
    }

    @GetMapping("/admin/staff")
    public String adminStaff(Model model) {
        model.addAttribute("currentPage", "staff");
        model.addAttribute("userName", "Super Admin");
        model.addAttribute("staffList", Collections.emptyList());
        model.addAttribute("departments", Collections.emptyList());
        return "admin/staff";
    }

    @GetMapping("/admin/faq")
    public String adminFaq(Model model) {
        model.addAttribute("currentPage", "faq");
        model.addAttribute("userName", "Super Admin");
        model.addAttribute("faqList", Collections.emptyList());
        return "admin/faq";
    }

    @GetMapping("/admin/reports")
    public String adminReports(Model model) {
        model.addAttribute("currentPage", "reports");
        model.addAttribute("userName", "Super Admin");
        model.addAttribute("report", Map.of(
            "totalVisits", 342,
            "totalRevenue", "125,500,000đ",
            "totalInsurance", "98,200,000đ",
            "aiAccuracy", "91.2%"
        ));
        model.addAttribute("topDepartments", Collections.emptyList());
        return "admin/reports";
    }
    
    // --- Public ---
    @GetMapping("/public/display")
    public String publicDisplay(Model model) {
        model.addAttribute("departmentName", "KHOA TIM MẠCH");
        model.addAttribute("departmentLocation", "Tòa B — Tầng 3 — Phòng 301-308");
        model.addAttribute("waitingList", Collections.emptyList());
        return "public/display";
    }
}
