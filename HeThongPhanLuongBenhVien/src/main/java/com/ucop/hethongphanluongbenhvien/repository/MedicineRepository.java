package com.ucop.hethongphanluongbenhvien.repository;

import com.ucop.hethongphanluongbenhvien.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, UUID> {
}
