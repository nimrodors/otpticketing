package com.example.otp.partner;

import com.example.otp.PartnerService;
import com.example.otp.responsedto.EventsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("partner/")
public class PartnerController {

    private static final Logger logger = LoggerFactory.getLogger(PartnerController.class);
    PartnerService partnerService;

    public PartnerController(PartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @Operation(
            summary = "List All Events.",
            description = "Fetch all events listed int the 'getEvents.json file'",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Events fetched successfully.",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error. Failed to load data.")
            }
    )

    @GetMapping("getEvents")
    public ResponseEntity<EventsResponse> getEvents() {
        try {
            EventsResponse eventsResponse = partnerService.getEvents("events/getEvents.json");
            logger.info("GET /partner/getEvents: Events fetched successfully.");
            return ResponseEntity.ok(eventsResponse);
        } catch (Exception e) {
            logger.error("GET /partner/getEvents: Failed to fetch events. Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();

        }
    }
}
