package com.example.otp.repository;

import com.example.otp.dto.user.UserBankCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBankCardRepository extends JpaRepository<UserBankCard, Integer> {

    @Query(value = "SELECT u.userId, u.email, d.deviceHash " +
            "FROM userbankcard c " +
            "JOIN users u ON c.userId = u.userId " +
            "JOIN userdevice d ON u.userId = d.userId " +
            "WHERE c.cardId = :cardId",
            nativeQuery = true)
    Optional<Object[]> findUserEmailAndIdByCardId(@Param("cardId") String cardId);

    List<UserBankCard> findAllByUserId(Long userId);

    @Query("SELECT u.userId FROM UserBankCard u WHERE u.cardId = :cardId")
    Optional<Integer> findUserIdByCardId(String cardId);

    @Query("SELECT c.cardnumber FROM UserBankCard c WHERE c.cardId = :cardId")
    Optional<String> findBankCardByCardId(String cardId);

    @Query("SELECT c.amount FROM UserBankCard c WHERE c.cardId = :cardId")
    int findAmountByCardId(String cardId);

    void deleteByCardId(String cardId);
}
