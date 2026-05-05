package com.thilakaswin.agrierp.service;

import com.thilakaswin.agrierp.entity.Crop;
import com.thilakaswin.agrierp.entity.LandZone;
import com.thilakaswin.agrierp.repository.CropRepository;
import com.thilakaswin.agrierp.repository.LandZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CropService {

    @Autowired
    private CropRepository cropRepository;

    @Autowired
    private LandZoneRepository landZoneRepository;

    public List<Crop> getAllCrops() {
        return cropRepository.findAll();
    }

    public Crop createCrop(Crop crop) {
        // 1. Fetch the REAL zone from the database using the ID from the payload
        if (crop.getLandZone() != null && crop.getLandZone().getZoneId() != null) {
            LandZone realZone = landZoneRepository.findById(crop.getLandZone().getZoneId())
                    .orElseThrow(() -> new RuntimeException("Land Zone not found"));

            // 2. Attach the real zone to your crop
            crop.setLandZone(realZone);
        }

        // 3. NOW it is safe to save!
        return cropRepository.save(crop);
    }

    public void deleteCrop(Long id) {
        cropRepository.deleteById(id);
    }

    public Crop updateCrop(Long id, Crop cropDetails) {
        Crop existingCrop = cropRepository.findById(id).orElseThrow(() -> new RuntimeException("Crop not found"));

        existingCrop.setCropVariety(cropDetails.getCropVariety());
        existingCrop.setAssetCategory(cropDetails.getAssetCategory());
        existingCrop.setQuantityPlanted(cropDetails.getQuantityPlanted());
        existingCrop.setStatus(cropDetails.getStatus());

        return cropRepository.save(existingCrop);
    }
}