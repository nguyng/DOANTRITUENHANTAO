package com.ucop.hethongphanluongbenhvien;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private static final String ADMIN_HASH = "$2a$10$xVXFF2NiTowRX52LNFt/SuI0xwTgJJ3asvlXWkpbo87YSfdQubemq";
    private static final String COMMON_USER_HASH = "$2a$10$dogvUePriTYmW0hIPyvLmuMz6shIsen5vDKa38V9MrT6lDNNEONeG";

    @Test
    @DisplayName("Khớp mật khẩu Admin@123 cho tài khoản ADMIN_001")
    void testAdminPasswordMatch() {
        assertTrue(encoder.matches("Admin@123", ADMIN_HASH), "Mật khẩu Admin@123 phải khớp với ADMIN_HASH");
        assertFalse(encoder.matches("123456", ADMIN_HASH), "Mật khẩu sai không được khớp");
        assertFalse(encoder.matches("wrongpass", ADMIN_HASH), "Mật khẩu sai không được khớp");
    }

    @Test
    @DisplayName("Khớp mật khẩu 123456 cho toàn bộ Y tá, Bác sĩ và Bệnh nhân")
    void testStaffAndPatientPasswordMatch() {
        assertTrue(encoder.matches("123456", COMMON_USER_HASH), "Mật khẩu 123456 phải khớp với COMMON_USER_HASH");
        assertFalse(encoder.matches("Admin@123", COMMON_USER_HASH), "Mật khẩu Admin@123 không được khớp cho tài khoản thường");
        assertFalse(encoder.matches("654321", COMMON_USER_HASH), "Mật khẩu sai không được khớp");
    }
}
