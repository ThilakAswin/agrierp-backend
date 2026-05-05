package com.thilakaswin.agrierp.dto;

import lombok.Data;

@Data
public class TaskDTO {
    private String taskId;
    private String taskType;   // e.g., "WATERING", "HARVESTING", "VACCINATION"
    private String assetName;  // e.g., "Banganapalli Mango"
    private String location;   // e.g., "North Mango Grove"
    private String urgency;    // e.g., "TODAY", "OVERDUE"
}