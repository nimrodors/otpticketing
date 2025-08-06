package com.example.otp.api.service;

import com.example.otp.dto.events.Events;
import com.example.otp.dto.events.EventsResponse;
import com.example.otp.dto.payment.PayRequest;
import com.example.otp.dto.reserve.ReserveResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {

    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);
    private final RestTemplate restTemplate;
    private static final String TICKET_URL = "http://localhost:8082/ticket";

    public ApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public EventsResponse getEvents() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        logger.debug("Fetching events from partner service...");

        ResponseEntity<EventsResponse> response = restTemplate.exchange(
                TICKET_URL + "/getEvents", HttpMethod.GET, entity, EventsResponse.class);
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
                TICKET_URL + "/getTicketEvent?EventId=" + eventId, HttpMethod.GET, entity, Events.class);

        if(response.getStatusCode().is2xxSuccessful()) {
            logger.info("Successfully fetched event {} from partner service.", eventId);
            return response.getBody();
        } else {
            logger.error("Failed to fetch event {} from partner service.", eventId);
            throw new RuntimeException("Failed to fetch event " + eventId + " from partner service.");
        }
    }

    public ReserveResponse pay(Long eventId, String seatId, String cardId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        PayRequest payRequest = new PayRequest(eventId, seatId, cardId);
        HttpEntity<PayRequest> entity = new HttpEntity<>(payRequest, headers);

        logger.debug("Paying for event {} with seat {} with card {}...", eventId, seatId, cardId);

        ResponseEntity<ReserveResponse> response = restTemplate.exchange(
                TICKET_URL + "/pay", HttpMethod.POST, entity, ReserveResponse.class);

        if(response.getStatusCode().is2xxSuccessful()) {
            logger.info("Successfully paid for event {} with seat {} with card {}.", eventId, seatId, cardId);
            return response.getBody();
        } else {
            logger.error("Failed to pay for event {} with seat {} with card {}.", eventId, seatId, cardId);
            throw new RuntimeException("Failed to pay for event " + eventId + " with seat " + seatId + " with card " + cardId + ".");
        }
    }
}
