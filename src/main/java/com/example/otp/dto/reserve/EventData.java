package com.example.otp.dto.reserve;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class EventData {
        private Long eventId;
        private List<Seat> seats;
}
