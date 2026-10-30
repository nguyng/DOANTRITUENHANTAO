package com.ucop.hethongphanluongbenhvien.service;

import com.ucop.hethongphanluongbenhvien.entity.User;
import com.ucop.hethongphanluongbenhvien.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Tìm kiếm ───────────────────────────────────────────────────────────────

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + id));
    }

    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    public Optional<User> findByBhytNumber(String bhytNumber) {
        return userRepository.findByBhytNumber(bhytNumber);
    }

    public Optional<User> findByCccd(String cccd) {
        return userRepository.findByCccd(cccd);
    }

    public Optional<User> findByAnyIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) return Optional.empty();
        return userRepository.findByCccd(identifier)
                .or(() -> userRepository.findByBhytNumber(identifier))
                .or(() -> userRepository.findByPhone(identifier))
                .or(() -> userRepository.findByEmail(identifier));
    }

    public List<User> findNursesByDepartment(UUID departmentId) {
        return userRepository.findByDepartmentIdAndRole(departmentId, User.Role.NURSE);
    }

    public List<User> findDoctorsByDepartment(UUID departmentId) {
        return userRepository.findByDepartmentIdAndRole(departmentId, User.Role.DOCTOR);
    }

    public List<User> findAllByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    public long countActiveStaff() {
        long nurses  = userRepository.findByRole(User.Role.NURSE).stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsActive())).count();
        long doctors = userRepository.findByRole(User.Role.DOCTOR).stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsActive())).count();
        return nurses + doctors;
    }

    // ── Ghi ────────────────────────────────────────────────────────────────────

    @Transactional
    public User registerPatient(User patient) {
        patient.setRole(User.Role.PATIENT);
        patient.setPasswordHash(passwordEncoder.encode(patient.getPasswordHash()));
        return userRepository.save(patient);
    }

    @Transactional
    public User createStaff(User staff) {
        staff.setPasswordHash(passwordEncoder.encode(staff.getPasswordHash()));
        return userRepository.save(staff);
    }

    @Transactional
    public void changePassword(UUID userId, String oldRaw, String newRaw) {
        User user = findById(userId);
        if (!passwordEncoder.matches(oldRaw, user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }
        user.setPasswordHash(passwordEncoder.encode(newRaw));
        userRepository.save(user);
    }

    @Transactional
    public User update(User updated) {
        User existing = findById(updated.getId());
        existing.setFullName(updated.getFullName());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setDob(updated.getDob());
        existing.setGender(updated.getGender());
        existing.setAddress(updated.getAddress());
        return userRepository.save(existing);
    }

    @Transactional
    public void toggleActive(UUID id) {
        User u = findById(id);
        u.setIsActive(!u.getIsActive());
        userRepository.save(u);
    }
}
