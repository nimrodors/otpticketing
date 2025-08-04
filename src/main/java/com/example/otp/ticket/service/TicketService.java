package com.example.otp.ticket.service;

import com.example.otp.dto.events.Events;
import com.example.otp.dto.events.EventsResponse;
import com.example.otp.ticket.JwtUtilCreateToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);
    private static final String PARTNER_URL = "http://localhost:8080/partner";

    private final RestTemplate restTemplate;
    private final JwtUtilCreateToken jwtUtilCreateToken;

    public TicketService(RestTemplate restTemplate, JwtUtilCreateToken jwtUtilCreateToken) {
        this.restTemplate = restTemplate;
        this.jwtUtilCreateToken = jwtUtilCreateToken;
    }

    public EventsResponse getEvents() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtUtilCreateToken.generateToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        logger.debug("Fetching events from partner service...");
        ResponseEntity<EventsResponse> response = restTemplate.exchange(
                PARTNER_URL + "/getEvents", HttpMethod.GET, entity, EventsResponse.class);

        if(response.getStatusCode().is2xxSuccessful()) {
            logger.info("Successfully fetched events from partner service.");
            return response.getBody();
        } else {
            logger.error("Failed to fetch events from partner service.");
            throw new RuntimeException("Failed to fetch events from partner service.");
        }
    }

    public Events getEvent(Long eventId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtUtilCreateToken.generateToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        logger.debug("Fetching event {} from partner service..., EventId: {}", eventId);
        ResponseEntity<Events> response = restTemplate.exchange(
                PARTNER_URL + "/getEventById?EventId=" + eventId, HttpMethod.GET, entity, Events.class);

        if(response.getStatusCode().is2xxSuccessful()) {
            logger.info("Successfully fetched event {} from partner service.", eventId);
            return response.getBody();
        } else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
            logger.error("Failed to fetch event {} from partner service.", eventId);
            throw new IllegalArgumentException("Invalid Event ID");
        } else {
            logger.error("Failed to fetch event {} from partner service.", eventId);
            throw new RuntimeException("Failed to fetch event " + eventId + " from partner service.");
        }
    }
}
