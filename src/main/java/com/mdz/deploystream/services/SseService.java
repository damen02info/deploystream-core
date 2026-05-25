package com.mdz.deploystream.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseService {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter registerClient(String deploymentId) {
        // Timeout set to 10 minutes (600,000 milliseconds)
        SseEmitter emitter = new SseEmitter(600_000L);

        emitter.onCompletion(() -> removeEmitter(deploymentId, "Conexión completada"));
        emitter.onTimeout(() -> removeEmitter(deploymentId, "Tiempo de espera agotado"));
        emitter.onError((ex) -> removeEmitter(deploymentId, "Error en la conexión"));

        emitters.put(deploymentId, emitter);
        log.info("SseEmitter registrado para el despliegue {}", deploymentId);

        try {
            emitter.send(SseEmitter.event().name("INIT").data("Conexión establecida para el despliegue " + deploymentId));
        } catch (Exception e) {
            log.error("Error al enviar evento de inicialización para el despliegue {}: {}", deploymentId, e.getMessage());
            removeEmitter(deploymentId, "Error al enviar evento de inicialización");
        }
        return emitter;
    }

    public void sendLogRealTime(String deploymentId, Object logPayload){
        SseEmitter emitter = emitters.get(deploymentId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("LOG").data(logPayload));
            } catch (Exception e) {
                log.error("Error al enviar log en tiempo real para el despliegue {}: {}", deploymentId, e.getMessage());
                removeEmitter(deploymentId, "Error al enviar log en tiempo real");
            }
        } else {
            log.warn("No se encontró un SseEmitter para el despliegue {}. No se pudo enviar el log en tiempo real.", deploymentId);
        }
    }

    private void removeEmitter(String deploymentId, String reason) {
        if (emitters.remove(deploymentId) != null) {
            log.warn("SseEmitter eliminado para el despliegue {}. Razón: {}", deploymentId, reason);
        }
    }
}
