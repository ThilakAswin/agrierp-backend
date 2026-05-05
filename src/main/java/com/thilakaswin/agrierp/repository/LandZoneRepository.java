package com.thilakaswin.agrierp.repository;

import com.thilakaswin.agrierp.entity.LandZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LandZoneRepository extends JpaRepository<LandZone, Long> {
    // Just by extending JpaRepository, Spring writes all your CRUD SQL for you!
}