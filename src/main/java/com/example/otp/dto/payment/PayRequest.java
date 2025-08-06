package com.example.otp.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayRequest {

    private Long eventId;
    private String seatId;
    private String cardId;

    public PayRequest(Long eventId, String seatId, String cardId) {
        this.eventId = eventId;
        this.seatId = seatId;
        this.cardId = cardId;
    }
}
