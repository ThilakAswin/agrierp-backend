package com.thilakaswin.agrierp.controller;

import com.thilakaswin.agrierp.entity.Crop;
import com.thilakaswin.agrierp.repository.CropRepository;
import com.thilakaswin.agrierp.service.CropService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.thilakaswin.agrierp.entity.AssetStatus;


import java.util.List;

@RestController
@RequestMapping("/api/crops")
@CrossOrigin(origins = "http://localhost:5173")
public class CropController {

    // Notice we changed this from Repository to Service!
    @Autowired
    private CropService cropService;

    @Autowired
    private CropRepository cropRepository;

    @GetMapping
    public List<Crop> getAllCrops() {
        return cropService.getAllCrops();
    }

    @PostMapping
    public Crop createCrop(@RequestBody Crop crop) {
        // Hand the data to the service layer so it can do the heavy lifting
        return cropService.createCrop(crop);
    }

    @DeleteMapping("/{id}")
    public void deleteCrop(@PathVariable Long id) {
        cropService.deleteCrop(id);
    }

    @PutMapping("/{id}")
    public Crop updateCrop(@PathVariable Long id, @RequestBody Crop cropDetails) {
        Crop existingCrop = cropRepository.findById(id).orElseThrow();

        // ... your existing updates (cropVariety, status, etc.) ...
        existingCrop.setStatus(cropDetails.getStatus());
        existingCrop.setIrrigationFrequencyDays(cropDetails.getIrrigationFrequencyDays());
        existingCrop.setExpectedHarvestDate(cropDetails.getExpectedHarvestDate());

        // ---> NEW: ADD THESE TWO LINES <---
        existingCrop.setYieldQuantity(cropDetails.getYieldQuantity());
        existingCrop.setTotalRevenue(cropDetails.getTotalRevenue());
        // -----------------------------------

        return cropRepository.save(existingCrop);
    }
    @PostMapping("/{id}/harvest-partial")
    public org.springframework.http.ResponseEntity<?> partialHarvest(
            @PathVariable Long id,
            @RequestBody com.thilakaswin.agrierp.dto.TaskCompletionDTO harvestRequest) {

        Crop existing = cropRepository.findById(id).orElseThrow();

        // Convert the incoming String status (SOLD/HARVESTED) to the Enum type
        AssetStatus targetStatus = AssetStatus.valueOf(harvestRequest.getStatus());

        int amountToHarvest = harvestRequest.getYieldQuantity().intValue();

        if (amountToHarvest >= existing.getQuantityPlanted()) {
            // FULL SALE/HARVEST
            existing.setStatus(targetStatus); // USE THE DYNAMIC STATUS
            existing.setYieldQuantity(harvestRequest.getYieldQuantity());
            existing.setTotalRevenue(harvestRequest.getTotalRevenue());
            cropRepository.save(existing);
        } else {
            // PARTIAL SALE/HARVEST: Split!
            Crop harvestedBatch = new Crop();
            harvestedBatch.setCropVariety(existing.getCropVariety());
            harvestedBatch.setAssetCategory(existing.getAssetCategory());
            harvestedBatch.setLandZone(existing.getLandZone());
            harvestedBatch.setDateAcquired(existing.getDateAcquired());
            harvestedBatch.setIrrigationFrequencyDays(existing.getIrrigationFrequencyDays());

            harvestedBatch.setQuantityPlanted(amountToHarvest);
            harvestedBatch.setYieldQuantity(harvestRequest.getYieldQuantity());
            harvestedBatch.setTotalRevenue(harvestRequest.getTotalRevenue());
            harvestedBatch.setStatus(targetStatus); // USE THE DYNAMIC STATUS
            cropRepository.save(harvestedBatch);

            // Reduce original field size
            existing.setQuantityPlanted(existing.getQuantityPlanted() - amountToHarvest);
            cropRepository.save(existing);
        }
        return org.springframework.http.ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/split-status")
    @Transactional // Important! This ensures either both updates happen or neither does
    public org.springframework.http.ResponseEntity<?> splitAssetStatus(
            @PathVariable Long id,
            @RequestBody com.thilakaswin.agrierp.dto.TaskCompletionDTO request) {

        Crop currentAsset = cropRepository.findById(id).orElseThrow();
        int qtyToMove = request.getYieldQuantity().intValue();
        AssetStatus targetStatus = AssetStatus.valueOf(request.getStatus());

        // 1. Check if a "Merge Target" already exists in the same zone
        java.util.Optional<Crop> mergeTarget = cropRepository.findAll().stream()
                .filter(c -> c.getLandZone().getZoneId().equals(currentAsset.getLandZone().getZoneId()))
                .filter(c -> c.getCropVariety().equals(currentAsset.getCropVariety()))
                .filter(c -> c.getStatus().equals(targetStatus))
                .filter(c -> !c.getAssetId().equals(id)) // Don't merge with itself
                .findFirst();

        if (mergeTarget.isPresent()) {
            // --- MERGE LOGIC ---
            Crop target = mergeTarget.get();
            target.setQuantityPlanted(target.getQuantityPlanted() + qtyToMove);

            // If moving money data (Sold/Harvested)
            if (request.getTotalRevenue() != null) {
                double existingRev = target.getTotalRevenue() != null ? target.getTotalRevenue() : 0;
                target.setTotalRevenue(existingRev + request.getTotalRevenue());
            }

            cropRepository.save(target);

            // Reduce or Delete the source record
            if (qtyToMove >= currentAsset.getQuantityPlanted()) {
                cropRepository.delete(currentAsset);
            } else {
                currentAsset.setQuantityPlanted(currentAsset.getQuantityPlanted() - qtyToMove);
                cropRepository.save(currentAsset);
            }

        } else {
            // --- EXISTING SPLIT LOGIC (If no merge target exists) ---
            if (qtyToMove >= currentAsset.getQuantityPlanted()) {
                currentAsset.setStatus(targetStatus);
                if (request.getTotalRevenue() != null) currentAsset.setTotalRevenue(request.getTotalRevenue());
                cropRepository.save(currentAsset);
            } else {
                Crop splitBatch = new Crop();
                splitBatch.setCropVariety(currentAsset.getCropVariety());
                splitBatch.setAssetCategory(currentAsset.getAssetCategory());
                splitBatch.setLandZone(currentAsset.getLandZone());
                splitBatch.setDateAcquired(currentAsset.getDateAcquired());
                splitBatch.setIrrigationFrequencyDays(currentAsset.getIrrigationFrequencyDays());

                splitBatch.setQuantityPlanted(qtyToMove);
                splitBatch.setStatus(targetStatus);
                if (request.getTotalRevenue() != null) splitBatch.setTotalRevenue(request.getTotalRevenue());

                cropRepository.save(splitBatch);
                currentAsset.setQuantityPlanted(currentAsset.getQuantityPlanted() - qtyToMove);
                cropRepository.save(currentAsset);
            }
        }
        return org.springframework.http.ResponseEntity.ok().build();
    }
}