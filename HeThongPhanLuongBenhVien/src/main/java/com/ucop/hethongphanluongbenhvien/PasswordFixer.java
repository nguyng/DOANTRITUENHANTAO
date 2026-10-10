package com.ucop.hethongphanluongbenhvien;

import com.ucop.hethongphanluongbenhvien.entity.User;
import com.ucop.hethongphanluongbenhvien.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PasswordFixer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordFixer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("====== ĐANG CẬP NHẬT LẠI MẬT KHẨU TRONG DB ======");
        
        // Sửa mật khẩu Admin
        userRepository.findByBhytNumber("ADMIN_001").ifPresent(admin -> {
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            userRepository.save(admin);
            System.out.println("Đã sửa pass cho ADMIN_001 thành Admin@123");
        });

        // Sửa mật khẩu Bệnh nhân
        userRepository.findByBhytNumber("HN47900000001").ifPresent(patient -> {
            patient.setPasswordHash(passwordEncoder.encode("123456"));
            userRepository.save(patient);
            System.out.println("Đã sửa pass cho HN47900000001 thành 123456");
        });

        // Sửa mật khẩu các Y tá mẫu (mật khẩu: 123456)
        String[] staffCodes = {"YTA_NGA", "YTA_SAN", "YTA_TIMMACH", "YTA_TMH", "YTA_NHI",
                               "YTA_THANKINH", "YTA_MAT", "YTA_HOHAP", "YTA_TIEUHOA",
                               "YTA_DALIEU", "YTA_XUONGKHOP"};
        for (String code : staffCodes) {
            userRepository.findByBhytNumber(code).ifPresent(nurse -> {
                nurse.setPasswordHash(passwordEncoder.encode("123456"));
                userRepository.save(nurse);
                System.out.println("Đã sửa pass cho " + nurse.getBhytNumber() + " thành 123456");
            });
        }

        System.out.println("====== HOÀN TẤT CẬP NHẬT ======");
    }
}
