package com.example.skku2_backend.dailyrecord.dto;

import lombok.Getter;

@Getter
public class CorrelationResult {

    private final String event;
    private final String response;
    private final String lagDays;
    private final int occurrence;
    private final int total;
    private final double ratio;

    public CorrelationResult(String event, String response, String lagDays,
                              int occurrence, int total) {
        this.event = event;
        this.response = response;
        this.lagDays = lagDays;
        this.occurrence = occurrence;
        this.total = total;
        this.ratio = total == 0 ? 0.0 : Math.round((double) occurrence / total * 100) / 100.0;
    }
}
