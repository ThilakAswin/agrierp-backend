package com.thilakaswin.agrierp.service;

import com.thilakaswin.agrierp.entity.Livestock;
import com.thilakaswin.agrierp.entity.LandZone;
import com.thilakaswin.agrierp.repository.LivestockRepository;
import com.thilakaswin.agrierp.repository.LandZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LivestockService {

    @Autowired
    private LivestockRepository livestockRepository;

    @Autowired
    private LandZoneRepository landZoneRepository;

    public List<Livestock> getAllLivestock() {
        return livestockRepository.findAll();
    }

    public Livestock createLivestock(Livestock livestock) {
        // THE FIX: Fetch the real LandZone first
        if (livestock.getLandZone() != null && livestock.getLandZone().getZoneId() != null) {
            LandZone realZone = landZoneRepository.findById(livestock.getLandZone().getZoneId())
                    .orElseThrow(() -> new RuntimeException("Land Zone not found"));

            livestock.setLandZone(realZone);
        }

        return livestockRepository.save(livestock);
    }

    public void deleteLivestock(Long id) {
        livestockRepository.deleteById(id);
    }

    public Livestock updateLivestock(Long id, Livestock livestockDetails) {
        Livestock existing = livestockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livestock not found"));

        existing.setSpecies(livestockDetails.getSpecies());
        existing.setHeadCount(livestockDetails.getHeadCount());
        existing.setDailyFeedKg(livestockDetails.getDailyFeedKg());
        existing.setStatus(livestockDetails.getStatus());

        return livestockRepository.save(existing);
    }
}