package com.mdz.deploystream.repositories;

import com.mdz.deploystream.entities.DeploymentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeploymentLogRepository extends JpaRepository<DeploymentLog, Long> {
    Optional<DeploymentLog> findTopByDeploymentIdOrderByLogIdDesc(String deploymentId);

    @Transactional(readOnly = true)
    List<DeploymentLog> findByDeploymentIdAndLogIdGreaterThanOrderByLogIdAsc(String deploymentId, Long logId);
}
