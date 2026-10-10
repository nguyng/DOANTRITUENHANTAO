package com.ucop.hethongphanluongbenhvien.config;

import com.ucop.hethongphanluongbenhvien.entity.User;
import com.ucop.hethongphanluongbenhvien.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Triển khai UserDetailsService để Spring Security tải thông tin user từ DB.
 *
 * Thứ tự tra cứu username:
 *   1. Số điện thoại  (phone)   — ưu tiên nhất (nhân viên & bệnh nhân)
 *   2. Số BHYT        (bhytNumber)
 *   3. CCCD           (cccd)
 *   4. Email          (email)
 */
@Service
@RequiredArgsConstructor
public class HmsUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByPhone(username)
                .or(() -> userRepository.findByBhytNumber(username))
                .or(() -> userRepository.findByCccd(username))
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy tài khoản: " + username));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UsernameNotFoundException("Tài khoản đã bị khóa: " + username);
        }

        // Chuyển Role thành GrantedAuthority — Spring Security dùng tiền tố "ROLE_"
        String authority = "ROLE_" + user.getRole().name(); // VD: ROLE_PATIENT, ROLE_NURSE, ROLE_ADMIN

        // Nhân viên (NURSE/DOCTOR/ADMIN) đăng nhập bằng bhytNumber → trả về bhytNumber làm username
        // Bệnh nhân đăng nhập bằng phone hoặc bhytNumber → ưu tiên phone
        String resolvedUsername;
        if (user.getRole() == User.Role.PATIENT) {
            resolvedUsername = user.getPhone() != null ? user.getPhone()
                    : user.getBhytNumber() != null ? user.getBhytNumber()
                    : user.getCccd();
        } else {
            // NURSE, DOCTOR, ADMIN — dùng bhytNumber (mã nhân viên)
            resolvedUsername = user.getBhytNumber() != null ? user.getBhytNumber()
                    : user.getPhone() != null ? user.getPhone()
                    : user.getCccd();
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(resolvedUsername)
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority(authority)))
                .accountExpired(false)
                .accountLocked(!Boolean.TRUE.equals(user.getIsActive()))
                .credentialsExpired(false)
                .disabled(!Boolean.TRUE.equals(user.getIsActive()))
                .build();
    }
}
