package com.thilakaswin.agrierp.repository;

import com.thilakaswin.agrierp.entity.Livestock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LivestockRepository extends JpaRepository<Livestock, Long> {
}