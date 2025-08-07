package com.example.otp.api.controller;

import com.example.otp.api.service.ApiService;
import com.example.otp.dto.events.Events;
import com.example.otp.dto.events.EventsResponse;
import com.example.otp.dto.payment.PayRequest;
import com.example.otp.dto.reserve.ReserveResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);
    private final ApiService apiService;

    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    @Operation(
            summary = "Fetching all EVENTS.",
            description = "This endpoint is used to fetch all EVENTS.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Events fetched successfully."),
                    @ApiResponse(responseCode = "500", description = "Internal server error.")
            })
    @GetMapping("/getEvents")
    public ResponseEntity<EventsResponse> getEvents() {
        try {
            EventsResponse eventsResponse = apiService.getEvents();
            logger.info("GET /api/getEvents: Events fetched successfully.");
            return ResponseEntity.ok(eventsResponse);
        } catch (Exception e) {
            logger.error("GET /api/getEvents: Failed to fetch events. Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Fetching a specific EVENT BY ID.",
            description = "Retrieves the details of the specified event based on the EventId.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Event fetched successfully."),
                    @ApiResponse(responseCode = "500", description = "Internal server error.")
            })
    @GetMapping("/getEvent")
    public ResponseEntity<?> getEvent(@RequestParam("EventId") Long eventId) {
        try {
            Events eventResponse = apiService.getEvent(eventId);
            logger.info("GET /api/getEvent: Events fetched successfully.");
            return ResponseEntity.ok(eventResponse);
        } catch (IllegalArgumentException e) {
            logger.error("GET /api/getEvent: Failed to fetch event. Error: " + e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("errorCode", 20001);
            error.put("message", "Érvénytelen esemény ID");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("GET /api/getEvent: Failed to fetch event. Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @Operation(
            summary = "Pay and reserve a seat for a specific EVENT.",
            description = "Initiates payment and reservation for a specific event based on EventId, SeatId, and CardId.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reservation successful.",
                            content = @Content(schema = @Schema(implementation = ReserveResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input or payment failure.",
                            content = @Content(schema = @Schema(implementation = Map.class))
                    ),
                    @ApiResponse(responseCode = "500", description = "Internal server error.")
            })
    @PostMapping("/pay")
    public ResponseEntity<?> pay(@RequestBody PayRequest payRequest) {
        try {
            ReserveResponse reserveResponse = apiService.pay(payRequest.getEventId(), payRequest.getSeatId(), payRequest.getCardId());
            logger.info("POST /api/pay: Reservation successful, reservationId: {}", reserveResponse.getReservationId());
            return ResponseEntity.ok(reserveResponse);
        } catch (IllegalArgumentException e) {
            logger.error("POST /api/pay: Failed to process payment. Error: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("errorCode", e.getMessage().contains("Nincs token") ? 10050 : e.getMessage().contains("lejárt token") ? 10051 : e.getMessage().contains("bankkártya") ? 10010 : e.getMessage().contains("egyenleg") ? 10101 : e.getMessage().contains("esemény") ? 20001 : e.getMessage().contains("szék") ? 20002 : e.getMessage().contains("foglalt") ? 20010 : e.getMessage().contains("elkezdődött") ? 20011 : 20404);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("POST /api/pay: Failed to process payment. Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
