package com.example.otp.repositorytest;

import com.example.otp.core.CoreService;
import com.example.otp.dto.user.UserBankCard;
import com.example.otp.dto.user.UserToken;
import com.example.otp.repository.UserBankCardRepository;
import com.example.otp.repository.UserDeviceRepository;
import com.example.otp.repository.UserRepository;
import com.example.otp.repository.UserTokenRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class RepositoryTest {

    @Autowired
    private UserBankCardRepository userBankCardRepository;
    @Autowired
    private UserDeviceRepository userDeviceRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserTokenRepository userTokenRepository;
    @Autowired
    private CoreService coreService;
    @Autowired
    private EntityManager entityManager;

    @Test
    public void testCoreService() {
        String cardId = "C0002";

        Optional<Integer> userId = userBankCardRepository.findUserIdByCardId(cardId);

        assertTrue(userId.isPresent(), "Az Optional tartlamaz értéket");

        Optional<Object[]> userResult = userBankCardRepository.findUserEmailAndIdByCardId(cardId);
        assertTrue(userResult.isPresent());

        Optional<String> checkExistingToken = userTokenRepository.findTokenByUserIdJPQL(userId.get());
        assertTrue(checkExistingToken.isPresent());

        Optional<String> checkTokenWithToken = userTokenRepository.findByToken("testTokenValue2025");
        assertTrue(checkTokenWithToken.isPresent());
       }

       @Test
       public void testFindUserIdByCardId() {
           String cardId = "C0006";

           // Először betöltjük a teszt adatokat
           UserBankCard card = new UserBankCard();
           card.setUserId(6);
           card.setCardId(cardId);
           card.setCardnumber("3243424324890123456");
           card.setCvc(121);
           card.setName("Makk User");
           card.setAmount("500");
           card.setCurrency("USD");

           userBankCardRepository.save(card);
           Optional<Integer> userId = userBankCardRepository.findUserIdByCardId(cardId);
           assertTrue(userId.isPresent(), "User ID should be present for the given cardId");

       }

       @Test
       public void testValidateToken() {
        String cardId = "C0002";
        boolean tokenValid = coreService.validateToken(cardId);
        assertTrue(tokenValid, "Token should be valid");
       }

       @Test
       public void testAddTokenToDbAndAfterValidate() {
           UserToken newToken = new UserToken();
           newToken.setUserId(5000);
           newToken.setToken("testTokenValue2025");
           userTokenRepository.save(newToken);

           Optional<String> checkExistingToken = userTokenRepository.findTokenByUserId(5000);
           if(checkExistingToken.isPresent()) {
               assertEquals(checkExistingToken.get(), "testTokenValue2025", "Token should be present in the database");
           }
       }

       @Test
       public void testSaveWithEntityManager() {
           UserToken newToken = new UserToken();
           newToken.setUserId((7000));
           newToken.setToken("testTokenMakiValue2025");
           userTokenRepository.save(newToken);

           System.out.println("Before persist");
           entityManager.persist(newToken);
           System.out.println("After persist");

       }
}
