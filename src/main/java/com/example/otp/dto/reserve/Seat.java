package com.example.otp.dto.reserve;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Seat {
    private String id;
    private int price;
    private String currency;
    private boolean reserved;

}
