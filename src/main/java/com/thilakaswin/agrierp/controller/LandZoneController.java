package com.thilakaswin.agrierp.controller;

import com.thilakaswin.agrierp.entity.LandZone;
import com.thilakaswin.agrierp.service.LandZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
@CrossOrigin(origins = "http://localhost:5173") // Don't forget this for React!
public class LandZoneController {

    @Autowired
    private LandZoneService landZoneService;

    @GetMapping
    public List<LandZone> getAllZones() {
        return landZoneService.getAllZones();
    }

    @PostMapping
    public LandZone createZone(@RequestBody LandZone zone) {
        return landZoneService.createZone(zone);
    }

    // Update Endpoint (PUT request)
    @PutMapping("/{id}")
    public LandZone updateZone(@PathVariable Long id, @RequestBody LandZone zone) {
        return landZoneService.updateZone(id, zone);
    }

    // Delete Endpoint (DELETE request)
    @DeleteMapping("/{id}")
    public void deleteZone(@PathVariable Long id) {
        landZoneService.deleteZone(id);
    }
}