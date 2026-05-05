package com.thilakaswin.agrierp.service;

import com.thilakaswin.agrierp.dto.TaskDTO;
import com.thilakaswin.agrierp.entity.AssetStatus;
import com.thilakaswin.agrierp.entity.Crop;
import com.thilakaswin.agrierp.entity.Livestock;
import com.thilakaswin.agrierp.repository.CropRepository;
import com.thilakaswin.agrierp.repository.LivestockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {

    @Autowired private CropRepository cropRepository;
    @Autowired private LivestockRepository livestockRepository;

    public List<TaskDTO> getDailyTasks() {
        List<TaskDTO> tasks = new ArrayList<>();
        LocalDate today = LocalDate.now();

        List<Crop> crops = cropRepository.findAll();
        for (Crop crop : crops) {
            if ("ACTIVE".equals(crop.getStatus().name()) || "MATURING".equals(crop.getStatus().name())) {

                // HARVESTING
                if (crop.getExpectedHarvestDate() != null && !crop.getExpectedHarvestDate().isAfter(today.plusDays(7))) {
                    TaskDTO task = new TaskDTO();
                    task.setTaskId("CROP-HARV-" + crop.getAssetId());
                    task.setTaskType("HARVESTING");
                    task.setAssetName(crop.getCropVariety());
                    task.setLocation(crop.getLandZone() != null ? crop.getLandZone().getZoneName() : "Unassigned");
                    task.setUrgency(crop.getExpectedHarvestDate().isBefore(today) ? "OVERDUE" : "THIS WEEK");
                    tasks.add(task);
                }

                // WATERING (Now uses lastWateredDate first, falls back to dateAcquired)
                if (crop.getIrrigationFrequencyDays() != null && crop.getDateAcquired() != null) {
                    LocalDate baseDate = crop.getLastWateredDate() != null ? crop.getLastWateredDate() : crop.getDateAcquired();
                    LocalDate nextWatering = baseDate.plusDays(crop.getIrrigationFrequencyDays());

                    if (!nextWatering.isAfter(today)) {
                        TaskDTO task = new TaskDTO();
                        task.setTaskId("CROP-H2O-" + crop.getAssetId());
                        task.setTaskType("WATERING");
                        task.setAssetName(crop.getCropVariety());
                        task.setLocation(crop.getLandZone() != null ? crop.getLandZone().getZoneName() : "Unassigned");
                        task.setUrgency("TODAY");
                        tasks.add(task);
                    }
                }
            }
        }

        List<Livestock> animals = livestockRepository.findAll();
        for (Livestock animal : animals) {
            if ("ACTIVE".equals(animal.getStatus().name()) && animal.getLastVaccinationDate() != null) {
                long daysSinceVax = ChronoUnit.DAYS.between(animal.getLastVaccinationDate(), today);
                if (daysSinceVax > 180) {
                    TaskDTO task = new TaskDTO();
                    task.setTaskId("LIVE-VAX-" + animal.getAssetId());
                    task.setTaskType("VACCINATION");
                    task.setAssetName(animal.getSpecies() + " (" + animal.getHeadCount() + " heads)");
                    task.setLocation(animal.getLandZone() != null ? animal.getLandZone().getZoneName() : "Unassigned");
                    task.setUrgency("OVERDUE");
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    // --- NEW: THE COMPLETION ENGINE ---
    // Change the method signature to include the DTO
    public void completeTask(String taskId, com.thilakaswin.agrierp.dto.TaskCompletionDTO dto) {
        if (taskId.startsWith("CROP-H2O-")) {
            Long cropId = Long.parseLong(taskId.replace("CROP-H2O-", ""));
            Crop crop = cropRepository.findById(cropId).orElseThrow();
            crop.setLastWateredDate(LocalDate.now());
            cropRepository.save(crop);

        } else if (taskId.startsWith("CROP-HARV-")) {
            Long cropId = Long.parseLong(taskId.replace("CROP-HARV-", ""));
            Crop crop = cropRepository.findById(cropId).orElseThrow();
            crop.setStatus(AssetStatus.HARVESTED);

            // --- NEW: Save the money and yield if provided! ---
            if (dto != null) {
                crop.setYieldQuantity(dto.getYieldQuantity());
                crop.setTotalRevenue(dto.getTotalRevenue());
            }

            cropRepository.save(crop);

        } else if (taskId.startsWith("LIVE-VAX-")) {
            Long animalId = Long.parseLong(taskId.replace("LIVE-VAX-", ""));
            Livestock animal = livestockRepository.findById(animalId).orElseThrow();
            animal.setLastVaccinationDate(LocalDate.now());
            livestockRepository.save(animal);
        }
    }
}