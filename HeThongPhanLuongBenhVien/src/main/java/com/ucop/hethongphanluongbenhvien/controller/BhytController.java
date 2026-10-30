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
 * Controller xu ly Xac thuc Bao Hiem Y Te (BHYT).
 *
 * Sau khi chuyen sang dang nhap bang CCCD:
 *   - SecurityContext luu CCCD cua benh nhan lam username
 *   - applyBhyt() kiem tra so BHYT nhap vao co khop voi CCCD dang nhap khong
 *     (moi CCCD chi duoc lien ket voi dung 1 so BHYT trong DB)
 *
 * Endpoints:
 *   GET  /bhyt/check      - Trang kiem tra BHYT truc tiep (danh cho benh nhan/le tan)
 *   POST /api/bhyt/verify - REST API xac thuc ma BHYT realtime (AJAX)
 *   POST /api/bhyt/apply  - Ap dung gia giam BHYT khi benh nhan nhap so the
 */
@Controller
@RequiredArgsConstructor
public class BhytController {

    private final BhytVerificationService bhytVerificationService;
    private final UserRepository          userRepository;

    // ======================== Trang Kiem Tra BHYT ========================

    /**
     * Trang tra cuu BHYT de benh nhan / le tan nhap ma de kiem tra truc tiep.
     * SecurityContext cua PATIENT hien tai luu CCCD lam username.
     */
    @GetMapping("/bhyt/check")
    public String bhytCheckPage(Model model,
                                @RequestParam(required = false) String maThe,
                                @AuthenticationPrincipal UserDetails ud) {
        model.addAttribute("currentPage", "profile");

        // Neu co ma tren URL (GET voi param) -> xu ly luon
        if (maThe != null && !maThe.isBlank()) {
            BhytVerifyResult result = bhytVerificationService.verify(maThe);
            model.addAttribute("result", result);
            model.addAttribute("maThe",  maThe.trim().toUpperCase().replaceAll("\\s+", ""));
        }

        // Dien ho ten user dang dang nhap
        // SecurityContext luu CCCD cho PATIENT, bhytNumber cho nhan vien
        if (ud != null) {
            userRepository.findByCccd(ud.getUsername())
                    .or(() -> userRepository.findByBhytNumber(ud.getUsername()))
                    .or(() -> userRepository.findByPhone(ud.getUsername()))
                    .ifPresent(u -> model.addAttribute("userName", u.getFullName()));
        }

        return "patient/bhyt-check";
    }

    // ======================== REST API cho AJAX realtime ========================

    /**
     * Xac thuc ma BHYT - goi tu JavaScript (AJAX / fetch).
     * Tra ve JSON ket qua de hien thi realtime khi nguoi dung ga ma.
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
        // Tra 200 luon, client tu doc field "valid" de biet hop le hay khong
        return ResponseEntity.ok(result);
    }

    /**
     * Ap dung giam gia BHYT cho lan kham hien tai.
     * Quy trinh:
     *   1. Xac thuc dinh dang so BHYT (layer 1 BhytVerificationService)
     *   2. Tim user hien tai dang dang nhap qua CCCD (SecurityContext)
     *   3. Kiem tra so BHYT nhap vao co KHOP voi so BHYT da lien ket voi CCCD nay khong
     *      -> Neu khop: ap dung giam gia
     *      -> Neu khong khop: tu choi (tranh gian lan BHYT)
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
                    .body(Map.of("success", false, "message", "Chua dang nhap"));
        }

        String maThe = body.getOrDefault("maThe", "").trim();

        // Buoc 1: Xac thuc dinh dang so BHYT
        BhytVerifyResult result = bhytVerificationService.verify(maThe);
        if (!result.valid()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", result.errorMessage()));
        }

        // Buoc 2: Tim user hien tai qua CCCD (SecurityContext luu CCCD cho PATIENT)
        Optional<User> userOpt = userRepository.findByCccd(ud.getUsername())
                .or(() -> userRepository.findByPhone(ud.getUsername()))
                .or(() -> userRepository.findByBhytNumber(ud.getUsername()));

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", "Khong tim thay tai khoan"));
        }

        User user = userOpt.get();

        // Buoc 3: Kiem tra so huu BHYT
        // Moi CCCD chi duoc su dung dung so BHYT lien ket trong DB
        // Neu user chua co BHYT (bhytNumber == null): tu choi, phai dang ky qua admin truoc
        // Neu user da co BHYT: so sanh voi ma the nhap vao
        String existingBhyt = user.getBhytNumber();
        if (existingBhyt == null || existingBhyt.isBlank()) {
            return ResponseEntity.status(403)
                    .body(Map.of("success", false,
                            "message", "CCCD nay chua duoc lien ket voi so BHYT nao. Vui long lien he admin."));
        }

        if (!existingBhyt.equalsIgnoreCase(result.maThe())) {
            return ResponseEntity.status(403)
                    .body(Map.of("success", false,
                            "message", "So BHYT khong khop voi CCCD dang dang nhap. Khong the ap dung giam gia."));
        }

        // Buoc 4: Cap nhat thong tin BHYT (han su dung, muc huong, v.v.)
        bhytVerificationService.applyVerifyResult(user, result);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "success",      true,
                "message",      "Da xac nhan BHYT thanh cong. Giam gia se duoc ap dung.",
                "maThe",        result.maThe(),
                "tenDoiTuong",  result.tenDoiTuong(),
                "mucHuong",     result.mucHuong(),
                "denNgay",      result.denNgay() != null ? result.denNgay().toString() : "",
                "dungTuyen",    result.dungTuyen()
        ));
    }
}