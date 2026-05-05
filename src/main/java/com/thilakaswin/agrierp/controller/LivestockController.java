package com.thilakaswin.agrierp.controller;

import com.thilakaswin.agrierp.entity.Livestock;
import com.thilakaswin.agrierp.repository.LivestockRepository;
import com.thilakaswin.agrierp.service.LivestockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.thilakaswin.agrierp.entity.AssetStatus;

import java.util.List;

@RestController
@RequestMapping("/api/livestock")
@CrossOrigin(origins = "http://localhost:5173")
public class LivestockController {

    // Swapped to Service!
    @Autowired
    private LivestockService livestockService;

    @Autowired
    private LivestockRepository livestockRepository;

    @GetMapping
    public List<Livestock> getAllLivestock() {
        return livestockService.getAllLivestock();
    }

    @PostMapping
    public Livestock createLivestock(@RequestBody Livestock livestock) {
        return livestockService.createLivestock(livestock);
    }

    @DeleteMapping("/{id}")
    public void deleteLivestock(@PathVariable Long id) {
        livestockService.deleteLivestock(id);
    }

    @PutMapping("/{id}")
    public Livestock updateLivestock(@PathVariable Long id, @RequestBody Livestock livestockDetails) {
        Livestock existingLivestock = livestockRepository.findById(id).orElseThrow();

        // ... your existing updates ...
        existingLivestock.setStatus(livestockDetails.getStatus());

        // ---> NEW: ADD THIS LINE <---
        existingLivestock.setTotalRevenue(livestockDetails.getTotalRevenue());
        // ----------------------------

        return livestockRepository.save(existingLivestock);
    }

    @PostMapping("/{id}/sell")
    public org.springframework.http.ResponseEntity<?> sellLivestock(
            @PathVariable Long id,
            @RequestBody com.thilakaswin.agrierp.dto.SellLivestockDTO sellRequest) {

        Livestock existing = livestockRepository.findById(id).orElseThrow();

        // Check if it's a FULL sale or PARTIAL sale
        if (sellRequest.getHeadsSold() >= existing.getHeadCount()) {
            // FULL SALE
            existing.setStatus(AssetStatus.SOLD); // <-- CHANGED HERE
            existing.setTotalRevenue(sellRequest.getTotalRevenue());
            livestockRepository.save(existing);
        } else {
            // PARTIAL SALE: Split the record!

            // 1. Create a brand new record for the sold animals
            Livestock soldBatch = new Livestock();
            soldBatch.setSpecies(existing.getSpecies());
            soldBatch.setAssetCategory(existing.getAssetCategory());
            soldBatch.setDailyFeedKg(existing.getDailyFeedKg());
            soldBatch.setLandZone(existing.getLandZone());
            soldBatch.setDateAcquired(existing.getDateAcquired());
            soldBatch.setLastVaccinationDate(existing.getLastVaccinationDate());

            // Set the new split data
            soldBatch.setHeadCount(sellRequest.getHeadsSold());
            soldBatch.setStatus(AssetStatus.SOLD); // <-- CHANGED HERE
            soldBatch.setTotalRevenue(sellRequest.getTotalRevenue());
            livestockRepository.save(soldBatch);

            // 2. Reduce the headcount of the original active animals
            existing.setHeadCount(existing.getHeadCount() - sellRequest.getHeadsSold());
            livestockRepository.save(existing);
        }

        return org.springframework.http.ResponseEntity.ok().build();

    }

    @PostMapping("/{id}/split-status")
    @Transactional
    public org.springframework.http.ResponseEntity<?> splitLivestockStatus(
            @PathVariable Long id,
            @RequestBody com.thilakaswin.agrierp.dto.SellLivestockDTO request) {

        Livestock currentAnimal = livestockRepository.findById(id).orElseThrow();
        int headsToMove = request.getHeadsSold();
        AssetStatus targetStatus = AssetStatus.valueOf(request.getStatus());

        // 1. Search for a "Merge Target" (Same zone, same species, target status)
        java.util.Optional<Livestock> mergeTarget = livestockRepository.findAll().stream()
                .filter(l -> l.getLandZone().getZoneId().equals(currentAnimal.getLandZone().getZoneId()))
                .filter(l -> l.getSpecies().equalsIgnoreCase(currentAnimal.getSpecies()))
                .filter(l -> l.getStatus().equals(targetStatus))
                .filter(l -> !l.getAssetId().equals(id))
                .findFirst();

        if (mergeTarget.isPresent()) {
            // --- MERGE LOGIC: Combine with existing pack ---
            Livestock target = mergeTarget.get();
            target.setHeadCount(target.getHeadCount() + headsToMove);

            if (request.getTotalRevenue() != null) {
                double existingRev = target.getTotalRevenue() != null ? target.getTotalRevenue() : 0;
                target.setTotalRevenue(existingRev + request.getTotalRevenue());
            }

            livestockRepository.save(target);

            // Remove from or delete the source row
            if (headsToMove >= currentAnimal.getHeadCount()) {
                livestockRepository.delete(currentAnimal);
            } else {
                currentAnimal.setHeadCount(currentAnimal.getHeadCount() - headsToMove);
                livestockRepository.save(currentAnimal);
            }

        } else {
            // --- SPLIT LOGIC: Create a new pack if no target exists ---
            if (headsToMove >= currentAnimal.getHeadCount()) {
                currentAnimal.setStatus(targetStatus);
                if (request.getTotalRevenue() != null) currentAnimal.setTotalRevenue(request.getTotalRevenue());
                livestockRepository.save(currentAnimal);
            } else {
                Livestock splitBatch = new Livestock();
                splitBatch.setSpecies(currentAnimal.getSpecies());
                splitBatch.setAssetCategory(currentAnimal.getAssetCategory());
                splitBatch.setDailyFeedKg(currentAnimal.getDailyFeedKg());
                splitBatch.setLandZone(currentAnimal.getLandZone());
                splitBatch.setDateAcquired(currentAnimal.getDateAcquired());
                splitBatch.setLastVaccinationDate(currentAnimal.getLastVaccinationDate());

                splitBatch.setHeadCount(headsToMove);
                splitBatch.setStatus(targetStatus);
                if (request.getTotalRevenue() != null) splitBatch.setTotalRevenue(request.getTotalRevenue());

                livestockRepository.save(splitBatch);
                currentAnimal.setHeadCount(currentAnimal.getHeadCount() - headsToMove);
                livestockRepository.save(currentAnimal);
            }
        }
        return org.springframework.http.ResponseEntity.ok().build();
    }
}