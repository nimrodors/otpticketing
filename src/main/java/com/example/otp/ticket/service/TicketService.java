package com.example.otp.ticket.service;

import com.example.otp.dto.events.Events;
import com.example.otp.dto.events.EventsResponse;
import com.example.otp.dto.payment.PayRequest;
import com.example.otp.dto.events.EventResponse;
import com.example.otp.dto.reserve.ReserveRequest;
import com.example.otp.dto.reserve.ReserveResponse;
import com.example.otp.dto.events.Seat;
import com.example.otp.ticket.JwtUtilCreateToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;


@Service
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);
    private static final String PARTNER_URL = "http://localhost:8080/partner";
    private static final String CORE_URL = "http://localhost:8080/partner";

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

        //2. Eseménz időpontjának ellenörzése
        long currentTime = Instant.now().getEpochSecond();
        long startTime = Long.parseLong(event.getStartTimeStamp());
        if (currentTime < startTime) {
            throw new IllegalArgumentException("Event has started yet.");
        }

        //3. Szék létezésének és foglaltságának ellenörzése
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtUtilCreateToken.generateToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<EventResponse> response = null;
        try {
            response = restTemplate.exchange(
                    PARTNER_URL + "/reserve?EventId=" + eventId + "&SeatId=" + seatId, HttpMethod.POST, entity, EventResponse.class);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to reserve seat {} for event {}.", seatId, eventId);
            throw new IllegalArgumentException("Failed to reserve seat " + seatId + " for event " + eventId + ".");
        }

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalArgumentException("A külső rendszer nem elérhető: " + eventId);
        }

        boolean seatExist = false;
        boolean seatReserved = false;
        int seatPrice = 0;
        for(Seat seat : response.getBody().getData().getSeats()) {
            if (seat.getId().equals(seatId)) {
                seatExist = true;
                seatReserved = seat.isReserved();
                seatPrice = seat.getPrice();
                break;
            }
        }

        if (!seatExist) {
            throw new IllegalArgumentException("Invalid seat ID: " + seatId);
        } else if (seatReserved) {
            throw new IllegalArgumentException("Seat is already reserved.");
        } else if (seatPrice == 0) {
            throw new IllegalArgumentException("Seat price is 0.");
        }

        //4. User token es bankkártya ellenörzése
        headers.setContentType(MediaType.APPLICATION_JSON);
        PayRequest payRequest = new PayRequest(eventId, seatId, cardId);
        HttpEntity<PayRequest> coreEntity = new HttpEntity<>(payRequest, headers);
        ResponseEntity<?> coreResponse = null;

        try {
            coreResponse = restTemplate.exchange(
                    CORE_URL + "/validatePayment", HttpMethod.POST, coreEntity, Object.class);
        } catch (RestClientException e) {
            logger.error("Failed to validate payment for seat {} for event {}.", seatId, eventId);
            throw new IllegalArgumentException("Failed to validate payment for seat " + seatId + " for event " + eventId +
                    ". The CORE SYSTEM is NOt AVAILABLE.");
        }

        //5.Foglalás a PARTNER modulban
        headers.setContentType(MediaType.APPLICATION_JSON);
        ReserveRequest reserveRequest = new ReserveRequest();
        reserveRequest.setEventId(eventId);
        reserveRequest.setSeatId(seatId);

        HttpEntity<ReserveRequest> entityReserve = new HttpEntity<>(reserveRequest, headers);
        ResponseEntity<ReserveResponse> responseReserve = null;
        try {
            responseReserve = restTemplate.exchange(
                    PARTNER_URL + "/reserve", HttpMethod.POST, entityReserve, ReserveResponse.class);
        } catch (RestClientException e) {
            logger.error("Failed to reserve seat {} for event {}.", seatId, eventId);
            throw new IllegalArgumentException("Failed to reserve seat " + seatId + " for event " + eventId +
                    ". The PARTNER SYSTEM is NOt AVAILABLE.");
        }

        if (responseReserve.getStatusCode().is2xxSuccessful()) {
            logger.info("Successfully reserved seat {} for event {}.", seatId, eventId);
            return responseReserve.getBody();
        } else {
            throw new IllegalArgumentException("Failed to reserve seat " + seatId + " for event " + eventId +
                    ". The PARTNER SYSTEM is NOt AVAILABLE.");
        }
    }
}
