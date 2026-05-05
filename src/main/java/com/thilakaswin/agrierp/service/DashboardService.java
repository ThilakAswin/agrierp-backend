package com.thilakaswin.agrierp.service;

import com.thilakaswin.agrierp.dto.DashboardSummaryDTO;
import com.thilakaswin.agrierp.entity.Livestock;
import com.thilakaswin.agrierp.repository.CropRepository;
import com.thilakaswin.agrierp.repository.LandZoneRepository;
import com.thilakaswin.agrierp.repository.LivestockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private LandZoneRepository landZoneRepository;

    @Autowired
    private CropRepository cropRepository;

    @Autowired
    private LivestockRepository livestockRepository;

    public DashboardSummaryDTO getFarmSummary() {
        DashboardSummaryDTO summary = new DashboardSummaryDTO();

        // 1. Get the total number of zones (Spring Data JPA gives us .count() for free!)
        summary.setTotalZones(landZoneRepository.count());

        // 2. Get the total number of crops and timber planted
        summary.setTotalCropsAndTimber(cropRepository.count());

        // 3. Get the actual headcount of animals (adding up the Kangayam and Murrah numbers)
        List<Livestock> allLivestock = livestockRepository.findAll();
        long totalAnimals = 0;
        for (Livestock animal : allLivestock) {
            if (animal.getHeadCount() != null) {
                totalAnimals += animal.getHeadCount();
            }
        }
        summary.setTotalLivestockHeadcount(totalAnimals);

        return summary;
    }
}