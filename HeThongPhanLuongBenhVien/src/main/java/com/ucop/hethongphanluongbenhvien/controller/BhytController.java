package com.ucop.hethongphanluongbenhvien.controller;

import com.ucop.hethongphanluongbenhvien.entity.User;
import com.ucop.hethongphanluongbenhvien.repository.UserRepository;
import com.ucop.hethongphanluongbenhvien.service.BhytVerificationService;
import com.ucop.hethongphanluongbenhvien.service.BhytVerificationService.BhytVerifyResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Controller xử lý Xác thực Bảo Hiểm Y Tế (BHYT).
 *
 * Endpoints:
 *   GET  /bhyt/check      — Trang kiểm tra BHYT trực tiếp (dành cho bệnh nhân/lễ tân)
 *   POST /api/bhyt/verify — REST API xác thực mã BHYT realtime (AJAX)
 *   POST /api/bhyt/apply  — Áp dụng kết quả xác thực vào hồ sơ bệnh nhân
 */
@Controller
@RequiredArgsConstructor
public class BhytController {

    private final BhytVerificationService bhytVerificationService;
    private final UserRepository          userRepository;

    // ── Trang Kiểm Tra BHYT ──────────────────────────────────────────────────────

    /**
     * Trang tra cứu BHYT — bệnh nhân / lễ tân nhập mã để kiểm tra trực tiếp.
     */
    @GetMapping("/bhyt/check")
    public String bhytCheckPage(Model model,
                                @RequestParam(required = false) String maThe,
                                @AuthenticationPrincipal UserDetails ud) {
        model.addAttribute("currentPage", "profile");

        // Nếu có mã trên URL (GET với param) → xử lý luôn
        if (maThe != null && !maThe.isBlank()) {
            BhytVerifyResult result = bhytVerificationService.verify(maThe);
            model.addAttribute("result", result);
            model.addAttribute("maThe",  maThe.trim().toUpperCase().replaceAll("\\s+", ""));
        }

        // Điền họ tên user đang đăng nhập
        if (ud != null) {
            userRepository.findByBhytNumber(ud.getUsername())
                    .or(() -> userRepository.findByPhone(ud.getUsername()))
                    .ifPresent(u -> model.addAttribute("userName", u.getFullName()));
        }

        return "patient/bhyt-check";
    }

    // ── REST API — AJAX realtime ──────────────────────────────────────────────────

    /**
     * Xác thực mã BHYT — gọi từ JavaScript (AJAX / fetch).
     * Trả về JSON kết quả để hiển thị realtime khi người dùng gõ mã.
     *
     * Request:  POST /api/bhyt/verify
     * Body:     { "maThe": "DN479XXXXXXXXXX" }
     * Response: { "valid": true, "tenDoiTuong": "...", "mucHuong": 80, ... }
     */
    @PostMapping("/api/bhyt/verify")
    @ResponseBody
    public ResponseEntity<BhytVerifyResult> verifyApi(@RequestBody Map<String, String> body) {
        String maThe = body.getOrDefault("maThe", "").trim();
        BhytVerifyResult result = bhytVerificationService.verify(maThe);
        // Trả 200 luôn, client tự đọc field "valid" để biết hợp lệ hay không
        return ResponseEntity.ok(result);
    }

    /**
     * Áp dụng kết quả xác thực vào hồ sơ bệnh nhân đang đăng nhập.
     * Dùng khi bệnh nhân muốn cập nhật mã BHYT vào tài khoản của mình.
     *
     * Request:  POST /api/bhyt/apply
     * Body:     { "maThe": "DN479XXXXXXXXXX" }
     */
    @PostMapping("/api/bhyt/apply")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> applyBhyt(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails ud) {

        if (ud == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Chưa đăng nhập"));
        }

        String maThe = body.getOrDefault("maThe", "").trim();
        BhytVerifyResult result = bhytVerificationService.verify(maThe);

        if (!result.valid()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", result.errorMessage()));
        }

        // Tìm user hiện tại
        Optional<User> userOpt = userRepository.findByBhytNumber(ud.getUsername())
                .or(() -> userRepository.findByPhone(ud.getUsername()));

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", "Không tìm thấy tài khoản"));
        }

        User user = userOpt.get();

        // Áp dụng thông tin BHYT vào hồ sơ
        user.setBhytNumber(result.maThe());
        bhytVerificationService.applyVerifyResult(user, result);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "success",      true,
                "message",      "Đã cập nhật thông tin BHYT thành công",
                "maThe",        result.maThe(),
                "tenDoiTuong",  result.tenDoiTuong(),
                "mucHuong",     result.mucHuong(),
                "denNgay",      result.denNgay() != null ? result.denNgay().toString() : "",
                "dungTuyen",    result.dungTuyen()
        ));
    }
}
