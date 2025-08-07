package com.example.otp.ticket.service;

import com.example.otp.dto.events.Events;
import com.example.otp.dto.events.EventsResponse;
import com.example.otp.dto.payment.PayRequest;
import com.example.otp.dto.events.EventResponse;
import com.example.otp.dto.reserve.ReserveRequest;
import com.example.otp.dto.reserve.ReserveResponse;
import com.example.otp.dto.events.Seat;
import com.example.otp.repository.UserBankCardRepository;
import com.example.otp.ticket.JwtUtilCreateToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;


@Service
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);
    private static final String PARTNER_URL = "http://localhost:8080/partner";
    private static final String CORE_URL = "http://localhost:8082/core";

    private final RestTemplate restTemplate;
    private final JwtUtilCreateToken jwtUtilCreateToken;
    private final UserBankCardRepository userBankCardRepository;

    public TicketService(RestTemplate restTemplate, JwtUtilCreateToken jwtUtilCreateToken, UserBankCardRepository userBankCardRepository) {
        this.restTemplate = restTemplate;
        this.jwtUtilCreateToken = jwtUtilCreateToken;
        this.userBankCardRepository = userBankCardRepository;
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

    public ReserveResponse pay(Long eventId, String seatId, String cardId) {
        //Esemény létezése
        Events event;
        try {
            event = getEvent(eventId);
            if (event == null) {
                throw new IllegalArgumentException("Invalid Event ID: " + eventId);
            }
        } catch (Exception e) {
            logger.error("Failed to fetch event {} from partner service.", eventId);
            throw new IllegalArgumentException("Invalid Event ID");
        }

        //2. Esemény időpontjának ellenörzése
        long currentTime = Instant.now().getEpochSecond();
        long startTime = Long.parseLong(event.getStartTimeStamp());
        if (currentTime < startTime) {
            throw new IllegalArgumentException("Event has started yet.");
        }

        //3. Szék létezésének és foglaltságának ellenörzése
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtUtilCreateToken.generateToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<EventResponse> eventResponse;
        try {
            eventResponse = restTemplate.exchange(
                    PARTNER_URL + "/getEventByEventId?eventId=" + eventId, HttpMethod.GET, entity, EventResponse.class);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to reserve seat {} for event {}.", seatId, eventId);
            throw new IllegalArgumentException("Failed to reserve seat " + seatId + " for event " + eventId + ".");
        }

        if (!eventResponse.getStatusCode().is2xxSuccessful() || eventResponse.getBody() == null) {
            throw new IllegalArgumentException("A külső rendszer nem elérhető: " + eventId);
        }

        boolean seatExist = false;
        boolean seatReserved = false;
        int seatPrice = 0;
        for(Seat seat : eventResponse.getBody().getData().getSeats()) {
            if (seat.getId().equals(seatId)) {
                seatExist = true;
                seatReserved = seat.isReserved();
                seatPrice = seat.getPrice();
                break;
            }
        }

        int eventPrice = userBankCardRepository.findAmountByCardId(cardId);

        if (!seatExist) {
            throw new IllegalArgumentException("Invalid seat ID: " + seatId);
        } else if (seatReserved) {
            throw new IllegalArgumentException("Seat is already reserved.");
        } else if (seatPrice > eventPrice) {
            throw new IllegalArgumentException("You havr no enough money for this seat. You have " + eventPrice + " euros, but the seat price is " + seatPrice + " euros.");
        }

        //4. User token es bankkártya ellenörzése
        //headers.setContentType(MediaType.APPLICATION_JSON);
        PayRequest payRequest = new PayRequest(eventId, seatId, cardId);
        HttpEntity<PayRequest> coreEntity = new HttpEntity<>(payRequest, headers);
        ResponseEntity<Boolean> coreResponse;

        try {
            coreResponse = restTemplate.exchange(
                    CORE_URL + "/validatePayment/" + cardId, HttpMethod.POST, coreEntity, Boolean.class);
        } catch (RestClientException e) {
            logger.error("Failed to validate payment for seat {} for event {}.", seatId, eventId);
            throw new IllegalArgumentException("Failed to validate payment for seat " + seatId + " for event " + eventId +
                    ". The CORE SYSTEM is NOt AVAILABLE.");
        }

        //5.Foglalás a PARTNER modulban
        ReserveRequest reserveRequest = new ReserveRequest();
        reserveRequest.setEventId(eventId);
        reserveRequest.setSeatId(seatId);
        headers.set("Authorization", "Bearer " + jwtUtilCreateToken.generateToken());
        HttpEntity<ReserveRequest> entityReserve = new HttpEntity<>(reserveRequest, headers);
        ResponseEntity<ReserveResponse> responseReserve;

        try {
            logger.debug("Sending reserve request to PARTNER service...");
            responseReserve = restTemplate.exchange(
                    PARTNER_URL + "/reserve", HttpMethod.POST, entityReserve, ReserveResponse.class);

            if(!responseReserve.getStatusCode().is2xxSuccessful()) {
                logger.error("Failed to reserve seat {} for event {}.", seatId, eventId);
                throw new IllegalArgumentException("Failed to reserve seat " + seatId + " for event " + eventId +
                        ". The PARTNER SYSTEM is NOt AVAILABLE.");
            }

            logger.debug("Reserve request sent to PARTNER service.");
            return responseReserve.getBody();
        } catch (RestClientException e) {
            logger.error("Failed to reserve seat {} for event {}.", seatId, eventId);
            throw new IllegalArgumentException("Failed to reserve seat " + seatId + " for event " + eventId +
                    ". The PARTNER SYSTEM is NOt AVAILABLE.");
        }
    }
}
