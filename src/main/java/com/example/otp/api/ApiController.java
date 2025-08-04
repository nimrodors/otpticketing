package com.example.otp.api;

import com.example.otp.dto.events.Events;
import com.example.otp.dto.events.EventsResponse;
import com.example.otp.dto.reserve.EventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
}
