package com.ucop.hethongphanluongbenhvien;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGen {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("123456: " + encoder.encode("123456"));
        System.out.println("Admin@123: " + encoder.encode("Admin@123"));
    }
}
