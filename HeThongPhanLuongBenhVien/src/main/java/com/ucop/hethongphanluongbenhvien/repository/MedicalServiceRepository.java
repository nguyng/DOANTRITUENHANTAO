package com.ucop.hethongphanluongbenhvien.repository;

import com.ucop.hethongphanluongbenhvien.entity.MedicalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MedicalServiceRepository extends JpaRepository<MedicalService, UUID> {
}
