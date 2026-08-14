package com.mdz.deploystream.services;

import com.mdz.deploystream.entities.DeploymentLog;
import com.mdz.deploystream.repositories.DeploymentLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class JenkinsOrchestratorService {

    private final ConfigService configService;
    private final JenkinsRunnerService runnerService;

    public void runDeploymentProcess(String projectParam, String deploymentId, String colorParam) {
        runnerService.runJenkinsJobAsync(projectParam, deploymentId, colorParam);
    }


}
