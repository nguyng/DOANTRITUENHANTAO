package com.ucop.hethongphanluongbenhvien.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HmsController {

    @GetMapping("/hms")
    public String hmsIndex() {
        return "redirect:/hms/index.html";
    }
}
