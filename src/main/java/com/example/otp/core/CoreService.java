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
    private final UserRepository userRepository;
    private final UserDeviceRepository userDevicerepository;

    public CoreService(UserBankCardRepository userBankCardRepository, UserTokenRepository userTokenRepository, UserRepository userRepository, UserDeviceRepository userDevicerepository) {
        this.userBankCardRepository = userBankCardRepository;
        this.userTokenRepository = userTokenRepository;
        this.userRepository = userRepository;
        this.userDevicerepository = userDevicerepository;
    }

    public void validatePayment(Long eventId, String seatId, String cardId) {
        //1. Token ellenörzés
        boolean tokenValid = validateToken(cardId);

        if(!tokenValid) {
            throw new IllegalArgumentException("Invalid TOKEN!");
        }


    }

    public boolean validateToken(String cardId) {
        Optional<Object[]> userResult = userBankCardRepository.findUserEmailAndIdByCardId(cardId);
        if(!userResult.isPresent()) {
            throw new IllegalArgumentException("Card ID not found");
        }

        Object[] userData = userResult.get();
        Object[] innerData = (Object[]) userData[0];

        Integer userId = innerData[0] instanceof Integer ? (Integer) innerData[0] : null;
        String email = innerData[1] instanceof String ? (String) innerData[1] : null;
        String deviceHash = innerData[2] instanceof String ? (String) innerData[2] : null;

        if(userId == null || email == null || deviceHash == null) {
            throw new IllegalArgumentException("Invalid data types returned by query");
        }

        String combinedData = email + "&" + userId + "&" + String.join(".", deviceHash);

        String token = Base64.getEncoder().encodeToString(combinedData
                        .getBytes(StandardCharsets.UTF_8)).trim();
        Optional<String> checkExistingToken = userTokenRepository.findTokenByUserId(userId);
        String createTokenFromDb = checkExistingToken.get().replaceAll("\\s", "").trim();

        System.out.println(token);
        System.out.println(createTokenFromDb);
        return token.equals(createTokenFromDb);
    }
}
