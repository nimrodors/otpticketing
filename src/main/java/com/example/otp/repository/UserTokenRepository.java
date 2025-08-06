package com.example.otp.repository;

import com.example.otp.dto.user.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Integer>{
    @Query("SELECT t.token FROM UserToken t WHERE t.token = :token")
    Optional<String> findByToken(@Param("token") String token);

    @Query(value = "SELECT token FROM usertoken WHERE userId = :userId", nativeQuery = true)
    Optional<String> findTokenByUserId(@Param("userId") int userId);

    @Query("SELECT t.token FROM UserToken t WHERE t.userId = :userId")
    Optional<String> findTokenByUserIdJPQL(@Param("userId") int userId);
}
