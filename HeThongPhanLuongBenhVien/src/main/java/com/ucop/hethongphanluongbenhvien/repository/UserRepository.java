package com.ucop.hethongphanluongbenhvien.repository;

import com.ucop.hethongphanluongbenhvien.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByCccd(String cccd);
    Optional<User> findByBhytNumber(String bhytNumber);
    Optional<User> findByEmail(String email);
    
    List<User> findByRole(User.Role role);
    List<User> findByDepartmentIdAndRole(UUID departmentId, User.Role role);
}
