package com.example.otp.ticket;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtilCreateToken {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtilCreateToken.class);

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT token generálása a PARTNER modul számára.
     * @return A generált JWT token.
     */
    public String generateToken() {
        long now = System.currentTimeMillis();
        String token = Jwts.builder()
                .setIssuer("TICKET")
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 3600 * 1000))
                .signWith(getSigningKey())
                .compact();
        logger.debug("Generated JWT token: {}", token);
        return token;
    }
}
