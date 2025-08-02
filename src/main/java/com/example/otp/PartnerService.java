package com.example.otp;

import com.example.otp.partner.PartnerController;
import com.example.otp.responsedto.EventsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class PartnerService {

    private static final Logger logger = LoggerFactory.getLogger(PartnerController.class);

    public EventsResponse getEvents(String jsonPath) {
        logger.debug("Fetching events from json file: {}" + jsonPath);
        try {
            ClassPathResource resource = new ClassPathResource(jsonPath);
            ObjectMapper objectMapper = new ObjectMapper();

            EventsResponse eventsResponse = objectMapper.readValue(resource.getInputStream(), EventsResponse.class);
            logger.debug("Successfully fetched events from json file: {}", eventsResponse);
            return eventsResponse;
        } catch (Exception e) {
            logger.error("Failed to fetch events from json file: " + jsonPath, e);
            throw new RuntimeException("Failed to fetch events from json file: " + jsonPath, e);
        }
    }
}
