package com.example.otp.dto.events;

import com.example.otp.dto.reserve.EventResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class EventsResponse {
    @JsonProperty("data")
    private List<Events> data;
    private boolean success;
}
