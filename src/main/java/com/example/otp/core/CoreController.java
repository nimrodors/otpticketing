package com.example.otp.core;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/core")
public class CoreController {

    private static final Logger logger = LoggerFactory.getLogger(CoreController.class);
    private final CoreService coreService;

    public CoreController(CoreService coreService) {
        this.coreService = coreService;
    }
    @Operation(
            summary = "Validate payment with card ID",
            description = "Validates the payment using the provided card ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Validation result",
                            content = @Content(schema = @Schema(implementation = Boolean.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid cardId",
                            content = @Content(schema = @Schema(implementation = Boolean.class))
                    ),
                    @ApiResponse(responseCode = "500", description = "Internal server error.")
            })
    @PostMapping(path = "/validatePayment/{cardId}")
    public ResponseEntity<Boolean> pay(@Parameter(description = "Card ID to validate", required = true) @PathVariable String cardId) {
        logger.debug("Paying for with cardId {}...", cardId);
        if(cardId == null || cardId.isEmpty()) {
            logger.error("Failed to process validation. CardId is empty.");
            return ResponseEntity.badRequest().build();
        }
        System.out.println("CardId: " + cardId + "");
        try {
            boolean isValid = coreService.validatePayment(cardId);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            logger.error("Failed to process validation. Error: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
