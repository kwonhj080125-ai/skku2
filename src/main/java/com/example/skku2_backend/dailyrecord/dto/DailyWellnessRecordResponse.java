package com.example.skku2_backend.dailyrecord.dto;

import com.example.skku2_backend.dailyrecord.domain.DailyWellnessRecord;
import lombok.Getter;

@Getter
public class DailyWellnessRecordResponse {

    private final Long recordId;
    private final Long userId;
    private final String loggedDate;

    public DailyWellnessRecordResponse(DailyWellnessRecord record) {
        this.recordId = record.getRecordId();
        this.userId = record.getUserId();
        this.loggedDate = record.getLoggedDate().toString();
    }
}
