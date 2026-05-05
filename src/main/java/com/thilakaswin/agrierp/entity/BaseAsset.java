package com.thilakaswin.agrierp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
@Data
@MappedSuperclass
public abstract class BaseAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id")
    private Long assetId;

    // This is perfect! It will create 'zone_id' in both 'crops' and 'livestock' tables.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zone_id")
    private LandZone landZone;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_category")
    private AssetCategory assetCategory;

    @Enumerated(EnumType.STRING)
    private AssetStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "date_acquired")
    private LocalDate dateAcquired;
}