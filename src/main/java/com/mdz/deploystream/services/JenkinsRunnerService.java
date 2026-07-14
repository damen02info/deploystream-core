package com.mdz.deploystream.services;

import com.mdz.deploystream.entities.DeploymentLog;
import com.mdz.deploystream.repositories.DeploymentLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JenkinsRunnerService {

    private final ConfigService configService;
    private final DeploymentLogRepository logRepository;
    private final SseService sseService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${jenkins.url:http://localhost:8082}")
    private String jenkinsUrl;

    @Value("${jenkins.username:admin}")
    private String username;

    @Value("${jenkins.token}")
    private String token;

    @Value("${jenkins.job.name:portfolio-deploy-job}")
    private String jobName;

    @Async("asyncExecutor")
    public void runJenkinsJobAsync(String projectParam, String deploymentId, String colorParam) {
        log.info("Iniciando flujo asíncrono de despliegue en Jenkins. ID asignado: {}", deploymentId);

        try {
            saveAndBroadcastLog(deploymentId, "INFO", "Proceso de despliegue inicializado para: " + projectParam);
            saveAndBroadcastLog(deploymentId, "INFO", "Iniciando petición a la API de Jenkins...");

            String url = String.format("%s/job/%s/buildWithParameters", jenkinsUrl, jobName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            String auth = username + ":" + token;
            headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("DEPLOYMENT_ID", deploymentId);
            if (colorParam != null && !colorParam.isBlank()) {
                params.add("COLOR", colorParam);
            }

            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, new HttpEntity<>(params, headers), String.class);

            if (response.getStatusCode() != HttpStatus.CREATED) {
                throw new RuntimeException("Jenkins rechazó la petición: " + response.getStatusCode());
            }

            saveAndBroadcastLog(deploymentId, "INFO", "Jenkins Job aceptado. Estado: IN_PROGRESS");

            Long lastSeenLogId = logRepository.findTopByDeploymentIdOrderByLogIdDesc(deploymentId)
                    .map(DeploymentLog::getLogId)
                    .orElse(0L);

            boolean pipelineActivo = true;
            String terminalStatus = null;

            while (pipelineActivo) {
                Thread.sleep(2000);

                List<DeploymentLog> nuevosLogs =
                        logRepository.findByDeploymentIdAndLogIdGreaterThanOrderByLogIdAsc(deploymentId, lastSeenLogId);

                for (DeploymentLog nuevoLog : nuevosLogs) {
                    sseService.sendLogRealTime(deploymentId, nuevoLog);
                    lastSeenLogId = nuevoLog.getLogId();
                    Thread.sleep(1500);
                    String msg = nuevoLog.getMessage() == null ? "" : nuevoLog.getMessage().toUpperCase();

                    if (msg.contains("SUCCESS")) {
                        terminalStatus = "SUCCESS";
                        pipelineActivo = false;
                        break;
                    } else if (msg.contains("FAILURE")) {
                        terminalStatus = "FAILURE";
                        pipelineActivo = false;
                        break;
                    } else if (msg.contains("ABORTED")) {
                        terminalStatus = "ABORTED";
                        pipelineActivo = false;
                        break;
                    }
                }
            }

            /* 2 success error
            if ("SUCCESS".equals(terminalStatus)) {
                saveAndBroadcastLog(
                        deploymentId,
                        "INFO",
                        "Despliegue finalizado con éxito en infraestructura de destino. Estado: SUCCESS"
                );
            } else {
                saveAndBroadcastLog(
                        deploymentId,
                        "ERROR",
                        "Despliegue finalizado sin éxito. Estado: " + terminalStatus
                );
            }
        */
        } catch (Exception e) {
            saveAndBroadcastLog(deploymentId, "ERROR", "Fallo crítico en la comunicación con Jenkins: " + e.getMessage());
        } finally {
            configService.startAutomaticRollbackCountdown("#291f1a");
            log.info("Hilo asíncrono finalizado para el despliegue {}", deploymentId);
            if (colorParam != null && !colorParam.isBlank()) {
                configService.emitConfigUpdate("COLOR", colorParam);
            }
        }
    }

    private void saveAndBroadcastLog(String deploymentId, String level, String message) {
        DeploymentLog buildLog = DeploymentLog.builder()
                .deploymentId(deploymentId)
                .logLevel(level)
                .message(message)
                .logTimestamp(LocalDateTime.now())
                .build();

        logRepository.save(buildLog);
        sseService.sendLogRealTime(deploymentId, buildLog);
    }
}