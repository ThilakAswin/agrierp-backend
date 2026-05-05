package com.thilakaswin.agrierp.service;

import com.thilakaswin.agrierp.entity.LandZone;
import com.thilakaswin.agrierp.repository.LandZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

import java.util.List;

@Service
public class LandZoneService {

    @Autowired
    private LandZoneRepository landZoneRepository;

    public List<LandZone> getAllZones() {
        return landZoneRepository.findAll();
    }

    public LandZone createZone(LandZone zone) {
        return landZoneRepository.save(zone);
    }

    public void deleteZone(Long id) {
        // Check if the zone exists first
        if (!landZoneRepository.existsById(id)) {
            throw new RuntimeException("Zone not found with id: " + id);
        }
        landZoneRepository.deleteById(id);
    }

    public LandZone updateZone(Long id, LandZone zoneDetails) {
        LandZone existingZone = landZoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone not found with id: " + id));

        // Use the field names from your provided LandZone entity
        existingZone.setZoneName(zoneDetails.getZoneName());
        existingZone.setSizeAcres(zoneDetails.getSizeAcres()); // Fixed: matches sizeAcres field
        existingZone.setSoilType(zoneDetails.getSoilType());   // Fixed: matches soilType field

        return landZoneRepository.save(existingZone);
    }


}