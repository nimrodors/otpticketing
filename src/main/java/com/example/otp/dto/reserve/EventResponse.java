package com.example.otp.dto.reserve;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventResponse {
    private EventData data;
    private boolean success;
}

