package com.example.carins.web.dto;

import java.time.LocalDate;

public class CarHistoryEventDto {
    private String type;
    private LocalDate date;
    private String description;

    public CarHistoryEventDto() {}

    public CarHistoryEventDto(String type, LocalDate date, String description) {
        this.type = type;
        this.date = date;
        this.description = description;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

