package com.ucop.hethongphanluongbenhvien.controller;

import com.ucop.hethongphanluongbenhvien.entity.ChatMessage;
import com.ucop.hethongphanluongbenhvien.entity.Ticket;
import com.ucop.hethongphanluongbenhvien.entity.User;
import com.ucop.hethongphanluongbenhvien.repository.UserRepository;
import com.ucop.hethongphanluongbenhvien.service.ChatService;
import com.ucop.hethongphanluongbenhvien.service.DepartmentService;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PatientController {

    private final UserService       userService;
    private final TicketService     ticketService;
    private final ChatService       chatService;
    private final DepartmentService departmentService;
    private final UserRepository    userRepository;

    // ── Helpers ──────────────────────────────────────────────────────────────────

    /**
     * Tìm user theo username trong UserDetails.
     * Username có thể là phone, bhytNumber, cccd hoặc email tùy cách đăng nhập.
     */
    private Optional<User> currentUser(UserDetails userDetails) {
        if (userDetails == null) return Optional.empty();
        String u = userDetails.getUsername();
        return userRepository.findByPhone(u)
                .or(() -> userRepository.findByBhytNumber(u))
                .or(() -> userRepository.findByCccd(u))
                .or(() -> userRepository.findByEmail(u));
    }

    private void addUserAttrs(Model model, User user) {
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("currentUser", user);
    }

    // ── Chat ─────────────────────────────────────────────────────────────────────

    @GetMapping("/chat")
    public String chatPage(Model model, @AuthenticationPrincipal UserDetails ud) {
        model.addAttribute("currentPage", "chat");
        model.addAttribute("departments", departmentService.findActive());
        model.addAttribute("sessionId", "");
        model.addAttribute("messages", List.of());

        currentUser(ud).ifPresentOrElse(
                u -> {
                    addUserAttrs(model, u);
                    try {
                        var session = chatService.startSession(u);
                        model.addAttribute("sessionId", session.getId().toString());
                    } catch (Exception ex) {
                        log.warn("Không tạo được chat session: {}", ex.getMessage());
                        // sessionId giữ là "" — trang vẫn hiển thị, chức năng chat bị tắt
                        model.addAttribute("chatUnavailable", true);
                    }
                },
                () -> model.addAttribute("userName", "Bệnh nhân")
        );
        return "patient/chat";
    }

    @PostMapping("/chat/send")
    @ResponseBody
    public ChatService.ChatSendResult sendMessage(@RequestParam String sessionId,
                                                  @RequestParam String message) {
        return chatService.sendMessage(UUID.fromString(sessionId), message);
    }

    /** Bệnh nhân xác nhận đăng ký khám sau khi AI phân luồng */
    @PostMapping("/chat/confirm")
    public String confirmRegistration(@RequestParam String sessionId,
                                      @RequestParam UUID departmentId,
                                      @RequestParam(defaultValue = "M3") String priority,
                                      @RequestParam(required = false) String aiSummary,
                                      @RequestParam(required = false, defaultValue = "0") Integer confidence,
                                      RedirectAttributes ra) {
        Ticket ticket = chatService.confirmRegistration(
                UUID.fromString(sessionId),
                departmentId,
                Ticket.Priority.valueOf(priority),
                aiSummary,
                confidence
        );
        ra.addFlashAttribute("success", "Đăng ký thành công! Phiếu số: " + ticket.getTicketNumber());
        return "redirect:/ticket";
    }

    /** Tạo phiếu khám trực tiếp từ form (nếu không qua chat session) */
    @PostMapping("/tickets")
    public String createTicket(@RequestParam UUID departmentId,
                               @RequestParam(defaultValue = "M3") String priority,
                               @RequestParam(required = false) String aiSummary,
                               @AuthenticationPrincipal UserDetails ud,
                               RedirectAttributes ra) {
        currentUser(ud).ifPresent(u -> {
            Ticket ticket = ticketService.issueTicket(
                    u, departmentId, Ticket.Priority.valueOf(priority),
                    aiSummary != null ? aiSummary : "Đăng ký khám", 85
            );
            ra.addFlashAttribute("success", "Đăng ký thành công! Phiếu số: " + ticket.getTicketNumber());
        });
        return "redirect:/ticket";
    }

    // ── Phiếu khám ───────────────────────────────────────────────────────────────

    @GetMapping("/ticket")
    public String ticketPage(Model model, @AuthenticationPrincipal UserDetails ud) {
        model.addAttribute("currentPage", "ticket");
        currentUser(ud).ifPresentOrElse(u -> {
            addUserAttrs(model, u);
            // Lấy phiếu mới nhất còn active
            ticketService.findByPatient(u.getId()).stream()
                    .filter(t -> t.getStatus() == Ticket.Status.WAITING
                              || t.getStatus() == Ticket.Status.CALLED)
                    .findFirst()
                    .ifPresent(ticket -> {
                        int[] waitInfo = ticketService.calculateWaitInfo(ticket);
                        model.addAttribute("ticket", ticket);
                        model.addAttribute("queuePosition", waitInfo[0]);
                        model.addAttribute("estimatedWaitMinutes", waitInfo[1]);
                    });
        }, () -> model.addAttribute("userName", "Bệnh nhân"));
        return "patient/ticket";
    }

    @PostMapping("/tickets/{id}/cancel")
    public String cancelTicket(@PathVariable UUID id,
                               @AuthenticationPrincipal UserDetails ud,
                               RedirectAttributes ra) {
        currentUser(ud).ifPresent(u -> {
            try {
                ticketService.cancel(id, u.getId());
                ra.addFlashAttribute("success", "Đã hủy phiếu khám");
            } catch (RuntimeException e) {
                ra.addFlashAttribute("error", e.getMessage());
            }
        });
        return "redirect:/ticket";
    }

    // ── Hồ sơ ────────────────────────────────────────────────────────────────────

    @GetMapping("/profile")
    public String profilePage(Model model, @AuthenticationPrincipal UserDetails ud) {
        model.addAttribute("currentPage", "profile");
        currentUser(ud).ifPresentOrElse(u -> {
            addUserAttrs(model, u);
            model.addAttribute("user", u);
            // Kiểm tra BHYT sắp hết / đã hết hạn
            if (u.getBhytExpiryDate() != null) {
                model.addAttribute("bhytExpired",     u.getBhytExpiryDate().isBefore(LocalDate.now()));
                model.addAttribute("bhytExpiringSoon", u.getBhytExpiryDate().isBefore(LocalDate.now().plusDays(30)));
            }
        }, () -> model.addAttribute("userName", "Bệnh nhân"));
        return "patient/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User updated,
                                @AuthenticationPrincipal UserDetails ud,
                                RedirectAttributes ra) {
        currentUser(ud).ifPresent(u -> {
            updated.setId(u.getId());
            userService.update(updated);
            ra.addFlashAttribute("success", "Cập nhật hồ sơ thành công");
        });
        return "redirect:/profile";
    }

    // ── Lịch sử khám ─────────────────────────────────────────────────────────────

    @GetMapping("/visits")
    public String visitsPage(Model model, @AuthenticationPrincipal UserDetails ud) {
        model.addAttribute("currentPage", "visits");
        currentUser(ud).ifPresentOrElse(u -> {
            addUserAttrs(model, u);
            model.addAttribute("visits", ticketService.findByPatient(u.getId()));
        }, () -> model.addAttribute("userName", "Bệnh nhân"));
        return "patient/visits";
    }

    // ── Chỉ dẫn đường ────────────────────────────────────────────────────────────

    @GetMapping("/directions")
    public String directionsPage(Model model, @AuthenticationPrincipal UserDetails ud) {
        model.addAttribute("currentPage", "directions");
        currentUser(ud).ifPresent(u -> addUserAttrs(model, u));
        model.addAttribute("departments", departmentService.findActive());
        return "patient/directions";
    }
}
