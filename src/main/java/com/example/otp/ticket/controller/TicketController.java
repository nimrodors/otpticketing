package com.example.otp.ticket.controller;

import com.example.otp.dto.events.Events;
import com.example.otp.dto.events.EventsResponse;
import com.example.otp.dto.payment.PayRequest;
import com.example.otp.dto.reserve.ReserveResponse;
import com.example.otp.ticket.service.TicketService;
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
import java.util.Optional;

@RestController
@RequestMapping("/ticket")
public class TicketController {

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Operation(
            summary = "Fetching a specific EVENT.",
            description = "Retrieves the details of the specified event based on the EventId.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Event fetched successfully."),
                    @ApiResponse(responseCode = "500", description = "Internal server error.")
            })
    @GetMapping("/getEvents")
    public ResponseEntity<EventsResponse> getEvents() {
        try {
            EventsResponse eventsResponse = ticketService.getEvents();
            logger.info("GET /ticket/getEvents: Events fetched successfully.");
            return ResponseEntity.ok(eventsResponse);
        } catch (Exception e) {
            logger.error("GET /ticket/getEvents: Failed to fetch events. Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Fetching a specific EVENT.",
            description = "Retrieves the details of the specified event based on the EventId.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Event fetched successfully."),
                    @ApiResponse(responseCode = "500", description = "Internal server error.")
            })
    @GetMapping("/getTicketEvent")
    public ResponseEntity<?> getEvent(@RequestParam("EventId") Long eventId) {
        try {
            Optional<Events> eventResponse = Optional.ofNullable(ticketService.getEvent(eventId));
            logger.debug("Fetching event {} from partner service...", eventId);

            if(!eventResponse.isPresent()) {
                logger.error("GET /ticket/getEvent: Failed to fetch event. Event not found: {}", eventId);
                return ResponseEntity.notFound().build();
            }

            logger.info("GET /ticket/getEvent: Events fetched successfully.");
            return ResponseEntity.ok(eventResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("errorCode", 20001);
            error.put("message", "Érvénytelen esemény ID");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("GET /ticket/getEvent: Failed to fetch event. Error: " + e.getMessage());
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
            ReserveResponse reserveResponse =
                    ticketService.pay(payRequest.getEventId(), payRequest.getSeatId(), payRequest.getCardId());
            logger.info("POST /ticket/pay: Reservation successful, reservationId: {}", reserveResponse.getReservationId());
            return ResponseEntity.ok(reserveResponse);
        } catch (IllegalArgumentException e) {
            logger.error("POST /ticket/pay: Failed to process payment. Error: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("errorCode", e.getMessage().contains("esemény") ? 20001 :
                    e.getMessage().contains("Nincs Rendelkezésre álló szék") ? 20002 :
                            e.getMessage().contains("Ez a Szék már foglalt") ? 20010 :
                                    e.getMessage().contains("Az esemény már elkezdődött") ? 20011 :
                                            e.getMessage().contains("Külső rendszer nem elérhető") ? 20404 :
                                                    e.getMessage().contains("Token nem található") ? 10050 :
                                                            e.getMessage().contains("Token lejárt") ? 10051 :
                                                                    e.getMessage().contains("Bankártya Hiba") ? 10010 : 10101);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("POST /ticket/pay: Failed to process payment. Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
}
