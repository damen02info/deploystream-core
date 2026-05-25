package com.mdz.deploystream.controllers;

import com.mdz.deploystream.services.ConfigService;
import com.mdz.deploystream.services.JenkinsOrchestratorService;
import com.mdz.deploystream.services.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/deploy")
@RequiredArgsConstructor
public class DeploymentController {

    private final JenkinsOrchestratorService jenkinsOrchestratorService;
    private final ConfigService configService;
    private final SseService sseService;

    @PostMapping
    public ResponseEntity<Map<String, String>> triggerDeployment(@RequestBody Map<String, String> requestBody) {
        String projectName = requestBody.get("project");

        if (projectName == null || projectName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El parámetro 'project' es obligatorio."));
        }

        if (configService.isSystemLocked()) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(Map.of("error", "El sistema está bloqueado. Hay un despliegue en curso en Jenkins."));
        }

        jenkinsOrchestratorService.triggerJenkinsDeployment(projectName);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("message", "Despliegue solicitado correctamente. Procesando en segundo plano..."));
    }

    @GetMapping(value = "/stream/{deploymentId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamDeploymentLogs(@PathVariable String deploymentId) {
        return sseService.registerClient(deploymentId);
    }
}
