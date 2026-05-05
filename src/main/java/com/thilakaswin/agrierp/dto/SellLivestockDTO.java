package com.thilakaswin.agrierp.dto;

import lombok.Data;

@Data
public class SellLivestockDTO {
    private String status;       // Add this - it captures "ACTIVE", "SOLD", etc.
    private Integer headsSold;   // This is the quantity moving
    private Double totalRevenue; // This captures the money
}