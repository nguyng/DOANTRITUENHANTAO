package com.ucop.hethongphanluongbenhvien.service;

import com.ucop.hethongphanluongbenhvien.entity.Department;
import com.ucop.hethongphanluongbenhvien.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    // ── Đọc ────────────────────────────────────────────────────────────────────

    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    public List<Department> findActive() {
        return departmentRepository.findByIsActiveTrue();
    }

    public Department findById(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa: " + id));
    }

    public Department findByCode(String code) {
        return departmentRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã khoa: " + code));
    }

    // ── Ghi ────────────────────────────────────────────────────────────────────

    @Transactional
    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    @Transactional
    public void toggleActive(UUID id) {
        Department d = findById(id);
        d.setIsActive(!d.getIsActive());
        departmentRepository.save(d);
    }
}
