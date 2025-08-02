package com.example.otp.dto;

import lombok.Data;

@Data
public class Events {

    private Long eventId;
    private String title;
    private String location;
    private String startTimeStamp;
    private String endTimeStamp;
}
