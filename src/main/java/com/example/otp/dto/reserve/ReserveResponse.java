package com.example.otp.dto.reserve;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReserveResponse {
    private boolean success;
    private Long reservationId;

}
