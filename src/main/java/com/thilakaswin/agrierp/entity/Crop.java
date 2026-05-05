package com.thilakaswin.agrierp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "crops")
public class Crop extends BaseAsset {


    @Column(name = "crop_variety")
    private String cropVariety;

    @Column(name = "quantity_planted")
    private Integer quantityPlanted;

    @Column(name = "expected_harvest_date")
    private LocalDate expectedHarvestDate;

    @Column(name = "irrigation_frequency_days")
    private Integer irrigationFrequencyDays;

    @Column(name = "last_watered_date")
    private LocalDate lastWateredDate;

    @Column(name = "yield_quantity")
    private Double yieldQuantity;

    @Column(name = "total_revenue")
    private Double totalRevenue;
}