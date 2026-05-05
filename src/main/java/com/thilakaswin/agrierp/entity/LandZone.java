package com.thilakaswin.agrierp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "land_zones")
public class LandZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zone_id")
    private Long zoneId;

    @Column(name = "zone_name")
    private String zoneName;

    @Column(name = "size_acres")
    private Double sizeAcres;

    @Column(name = "soil_type")
    private String soilType;
}