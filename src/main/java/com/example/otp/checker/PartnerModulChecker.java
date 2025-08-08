package com.example.otp.checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PartnerModulChecker {

    private static final Logger logger = LoggerFactory.getLogger(PartnerModulChecker.class);
    private final RestTemplate restTemplate;

    public PartnerModulChecker(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean isAvailable() {
        logger.debug("Checking partner module availability...");
        String healthCheckUrl = "http://localhost:8080/actuator/health";
        HttpEntity<Void> requestEntity = new HttpEntity<>(new HttpHeaders());
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    healthCheckUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            logger.error("Partner module is not available. Error: {}", e.getMessage());
            return false;
        }
    }
}
