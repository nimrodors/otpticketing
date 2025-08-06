package com.example.otp.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayResponse {

    private boolean success;
    private Long reservationId; // Sikeres foglalás esetén
    private Integer errorCode; // Hiba esetén
    private String message;

    public PayResponse(boolean success, Long reservationId, Integer errorCode, String message) {
        this.success = success;
        this.reservationId = reservationId;
        this.errorCode = errorCode;
        this.message = message;
    }
}
