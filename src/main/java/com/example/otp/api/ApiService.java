package com.example.otp.api;

import com.example.otp.dto.events.Events;
import com.example.otp.dto.events.EventsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {

    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);
    private final RestTemplate restTemplate;
    private static final String PARTNER_URL = "http://localhost:8082/ticket";

    public ApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public EventsResponse getEvents() {
        HttpHeaders headers = new HttpHeaders();
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
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        logger.debug("Fetching event {} from partner service...", eventId);

        ResponseEntity<Events> response = restTemplate.exchange(
                PARTNER_URL + "/getTicketEvent?EventId=" + eventId, HttpMethod.GET, entity, Events.class);

        if(response.getStatusCode().is2xxSuccessful()) {
            logger.info("Successfully fetched event {} from partner service.", eventId);
            return response.getBody();
        } else {
            logger.error("Failed to fetch event {} from partner service.", eventId);
            throw new RuntimeException("Failed to fetch event " + eventId + " from partner service.");
        }
    }
}
