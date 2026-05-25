package com.mdz.deploystream.repositories;

import com.mdz.deploystream.entities.DeploymentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeploymentLogRepository extends JpaRepository<DeploymentLog, Long> {
}
