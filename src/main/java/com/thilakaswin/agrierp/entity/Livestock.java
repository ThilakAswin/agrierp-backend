package com.thilakaswin.agrierp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "livestock")
public class Livestock extends BaseAsset {


    @Column(name = "species")
    private String species;

    @Column(name = "head_count")
    private Integer headCount;

    @Column(name = "last_vaccination_date")
    private LocalDate lastVaccinationDate;

    @Column(name = "daily_feed_kg")
    private Double dailyFeedKg;

    @Column(name = "total_revenue")
    private Double totalRevenue;

}