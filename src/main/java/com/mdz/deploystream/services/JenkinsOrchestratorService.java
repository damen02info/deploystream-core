package com.mdz.deploystream.services;

import com.mdz.deploystream.entities.DeploymentLog;
import com.mdz.deploystream.repositories.DeploymentLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class JenkinsOrchestratorService {

    // private final RestClient restClient = RestClient.builder().build();
    private final ConfigService configService;
    private final JenkinsRunnerService runnerService;

    public void initDeploymentProcess(String projectParam, String deploymentId) {
        configService.setSystemLock(true);
        runnerService.runJenkinsJobAsync(projectParam, deploymentId);
    }


}
