package com.ucop.hethongphanluongbenhvien.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Các route redirect đơn giản — trang gốc và các trang không có controller riêng.
 * Toàn bộ logic đã được chuyển sang các Controller chuyên biệt:
 * AuthController, PatientController, NurseController, AdminController, PublicController.
 */
@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "redirect:/chat";
    }

}
