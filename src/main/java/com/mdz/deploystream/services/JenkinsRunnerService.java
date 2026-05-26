package com.mdz.deploystream.services;

import com.mdz.deploystream.entities.DeploymentLog;
import com.mdz.deploystream.repositories.DeploymentLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class JenkinsRunnerService {

    private final ConfigService configService;
    private final DeploymentLogRepository logRepository;
    private final SseService sseService;

    @Async("asyncExecutor")
    public void runJenkinsJobAsync(String projectParam, String deploymentId) {

        log.info("Iniciando flujo asíncrono de despliegue en Jenkins. ID asignado: {}", deploymentId);

        try {

            // Simulate initial processing time before calling Jenkins API. This can be removed once the actual API call is implemented.
            Thread.sleep(10000);

            saveAndBroadcastLog(deploymentId, "INFO", "Proceso de despliegue inicializado para: " + projectParam);

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
            configService.setSystemLock(false);
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
