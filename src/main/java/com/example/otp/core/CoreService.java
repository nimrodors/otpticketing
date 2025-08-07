package com.example.otp.core;

import com.example.otp.dto.user.UserToken;
import com.example.otp.repository.UserBankCardRepository;
import com.example.otp.repository.UserDeviceRepository;
import com.example.otp.repository.UserRepository;
import com.example.otp.repository.UserTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class CoreService {

    private final static Logger logger = LoggerFactory.getLogger(CoreService.class);

    private final UserBankCardRepository userBankCardRepository;
    private final UserTokenRepository userTokenRepository;

    public CoreService(UserBankCardRepository userBankCardRepository, UserTokenRepository userTokenRepository) {
        this.userBankCardRepository = userBankCardRepository;
        this.userTokenRepository = userTokenRepository;
    }

    public boolean validatePayment(String cardId) {
        try {
            //1. Token ellenörzés
            boolean tokenValid = validateToken(cardId);

            if(!tokenValid) {
                throw new IllegalArgumentException("Invalid TOKEN!");
            }

            boolean checkUserBankcard = checkUserBankCard(cardId);

            if(!checkUserBankcard) {
                throw new IllegalArgumentException("Invalid Bank Card!");
            }

            return checkUserBankcard && tokenValid;
        } catch (IllegalArgumentException e) {
            logger.error("Failed to validate payment. Error: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to validate payment. Error: " + e.getMessage());
        }
    }

    private boolean checkUserBankCard(String cardId) {
        Optional<String> userBankCard = userBankCardRepository.findBankCardByCardId(cardId);
        if(!userBankCard.isPresent()) {
            logger.error("Bank Card Not found!");
            return false;
        }
        return true;
    }

    public boolean validateToken(String cardId) {
        Optional<Object[]> userResult = userBankCardRepository.findUserEmailAndIdByCardId(cardId);
        if(!userResult.isPresent()) {
            logger.error("User not found!");
            return false;
        }

        Object[] userData = userResult.get();
        if(!(userData[0] instanceof Object[] )) {
            logger.error("Invalid data structure returned for cardId: {}", cardId);
            return false;
        }

        Object[] innerData = (Object[]) userData[0];

        Integer userId = null;
        String email = null;
        String deviceHash = null;
        if (innerData.length < 3  ||
                !(innerData[0] instanceof Integer)
                || !(innerData[1] instanceof String)
                || !(innerData[2] instanceof String))
            {
                logger.error("Invalid data types in user data for cardId: {}", cardId);
                return false;
        } else {
            userId = innerData[0] instanceof Integer ? (Integer) innerData[0] : null;
            email = innerData[1] instanceof String ? (String) innerData[1] : null;
            deviceHash = innerData[2] instanceof String ? (String) innerData[2] : null;

        }

        if(userId == null || email == null || deviceHash == null) {
            logger.error("User, email and device Hash not found!");
            return false;
        }

        String combinedData = email + "&" + userId + "&" + String.join(".", deviceHash);

        String token = Base64.getEncoder().encodeToString(combinedData
                        .getBytes(StandardCharsets.UTF_8)).trim();
        Optional<String> checkExistingToken = userTokenRepository.findTokenByUserId(userId);
        String createTokenFromDb = checkExistingToken.get().replaceAll("\\s", "").trim();

        logger.debug("Token: {}", token);
        logger.debug("Token from DB: {}", createTokenFromDb);

        return token.equals(createTokenFromDb);
    }
}
