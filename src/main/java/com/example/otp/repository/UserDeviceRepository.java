package com.example.otp.repository;

import com.example.otp.dto.user.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Integer> {
    @Query("SELECT d.deviceHash FROM UserDevice d WHERE d.userId = :userId")
    List<String> findDeviceHashesByUserId(@Param("userId") Long userId);
}
