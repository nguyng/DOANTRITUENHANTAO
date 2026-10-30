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
 * Trien khai UserDetailsService de Spring Security tai thong tin user tu DB.
 *
 * Thu tu tra cuu username (ho tro ca benh nhan lan nhan vien):
 *   1. So CCCD        (cccd)      - Benh nhan dang nhap bang CCCD
 *   2. So dien thoai  (phone)
 *   3. So BHYT        (bhytNumber)
 *   4. Email          (email)
 *
 * Username luu vao SecurityContext:
 *   - PATIENT: CCCD (de kiem tra so huu BHYT)
 *   - NURSE/DOCTOR/ADMIN: bhytNumber (ma nhan vien)
 */
@Service
@RequiredArgsConstructor
public class HmsUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByCccd(username)
                .or(() -> userRepository.findByPhone(username))
                .or(() -> userRepository.findByBhytNumber(username))
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Khong tim thay tai khoan: " + username));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UsernameNotFoundException("Tai khoan da bi khoa: " + username);
        }

        // Chuyen Role thanh GrantedAuthority - Spring Security dung tien to "ROLE_"
        String authority = "ROLE_" + user.getRole().name(); // VD: ROLE_PATIENT, ROLE_NURSE, ROLE_ADMIN

        // Benh nhan (PATIENT) dang nhap bang CCCD -> luu CCCD vao SecurityContext
        //   de controller co the tim user bang findByCccd(ud.getUsername())
        //   va kiem tra so huu BHYT (user.cccd phai khop voi BHYT dang ky)
        // Nhan vien (NURSE/DOCTOR/ADMIN) dang nhap bang bhytNumber (ma nhan vien)
        String resolvedUsername;
        if (user.getRole() == User.Role.PATIENT) {
            // Uu tien CCCD lam username chinh cua benh nhan
            resolvedUsername = user.getCccd() != null ? user.getCccd()
                    : user.getPhone() != null ? user.getPhone()
                    : user.getBhytNumber();
        } else {
            // NURSE, DOCTOR, ADMIN - dung bhytNumber (ma nhan vien)
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