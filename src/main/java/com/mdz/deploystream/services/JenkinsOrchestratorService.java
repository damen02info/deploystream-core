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
    private final DeploymentLogRepository logRepository;
    private final SseService sseService;
    private final ConfigService configService;

    public String initDeploymentProcess(String projectParam) {
        String deploymentId = UUID.randomUUID().toString().substring(0, 8);
        configService.setSystemLock(true);
        saveAndBroadcastLog(deploymentId, "INFO", "Proceso de despliegue inicializado para: " + projectParam);
        this.runJenkinsJobAsync(projectParam, deploymentId);
        return deploymentId;
    }

    @Async("asyncExecutor")
    public void runJenkinsJobAsync(String projectParam, String deploymentId) {
        log.info("Iniciando flujo asíncrono de despliegue en Jenkins. ID asignado: {}", deploymentId);

        try {

            // Simulate initial processing time before calling Jenkins API. This can be removed once the actual API call is implemented.
            Thread.sleep(2000);

            // Log the start of the deployment process
            saveAndBroadcastLog(deploymentId, "INFO", "Iniciando petición a la API de Jenkins para el proyecto: " + projectParam);

            // TODO - Implement the actual call to Jenkins API to trigger the job with the provided project parameter. URL from properties file.

            // Log the successful acceptance of the Jenkins job
            saveAndBroadcastLog(deploymentId, "INFO", "Jenkins Job aceptado. Estado: IN_PROGRESS");

            // Simulate time taken for Jenkins to process the job. This should be replaced with actual status checks.
            Thread.sleep(5000);

            // TODO - Implement polling mechanism to check Jenkins job status and log updates in real-time
            // while (job is running)..

            // End of the deployment process
            saveAndBroadcastLog(deploymentId, "INFO", "Despliegue finalizado con éxito en infraestructura de destino.");
        } catch (Exception e) {
            saveAndBroadcastLog(deploymentId, "ERROR", "Fallo crítico en la comunicación con Jenkins: " + e.getMessage());
        } finally {
            // Unlock the system to allow new deployments. This should ideally be done after confirming the deployment is fully completed,
            // either through a 'RollbackTask' or a Jenkins success webhook.
            log.info("Hilo asíncrono finalizado para el despliegue {}", deploymentId);
        }
    }

    private void saveAndBroadcastLog(String deploymentId, String level, String message) {
        DeploymentLog buildLog = DeploymentLog.builder()
                .deploymentId(deploymentId)
                .logLevel(level)
                .message(message)
                .logTimestamp(LocalDateTime.now())
                .build();

        // Save log to the database
        logRepository.save(buildLog);

        // Send log to clients in real-time via SSE
        sseService.sendLogRealTime(deploymentId, buildLog);
    }

}
