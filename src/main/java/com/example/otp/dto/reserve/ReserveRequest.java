package com.example.otp.dto.reserve;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReserveRequest {
    private String seatId;
    private Long eventId;
}
