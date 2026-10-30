package com.ucop.hethongphanluongbenhvien.controller;

import com.ucop.hethongphanluongbenhvien.entity.User;
import com.ucop.hethongphanluongbenhvien.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // ── GET ──────────────────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/change-password")
    public String changePasswordPage(Model model,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            userService.findByAnyIdentifier(userDetails.getUsername())
                    .ifPresent(u -> model.addAttribute("userName", u.getFullName()));
        }
        return "auth/change-password";
    }

    // ── POST: Đổi mật khẩu ───────────────────────────────────────────────────────

    @PostMapping("/change-password")
    public String doChangePassword(@AuthenticationPrincipal UserDetails userDetails,
                                   @RequestParam String oldPassword,
                                   @RequestParam String newPassword,
                                   @RequestParam String confirmPassword,
                                   RedirectAttributes ra) {
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Mật khẩu xác nhận không khớp");
            return "redirect:/change-password";
        }
        try {
            User user = userService.findByAnyIdentifier(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
            userService.changePassword(user.getId(), oldPassword, newPassword);
            ra.addFlashAttribute("success", "Đổi mật khẩu thành công");
            return "redirect:/";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/change-password";
        }
    }
}
