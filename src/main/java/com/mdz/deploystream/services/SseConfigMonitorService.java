package com.mdz.deploystream.services;

import com.mdz.deploystream.entities.AppConfig;
import com.mdz.deploystream.events.AppConfigUpdateEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseConfigMonitorService {

    // List of active SSE emitters (clients) subscribed to config updates
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // Method to register a new client for config updates
    public SseEmitter registerDashboardClient() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        return emitter;
    }

    // Listen for AppConfigUpdatedEvent and broadcast the new config to all subscribed clients
    @EventListener
    public void onConfigUpdate(AppConfigUpdateEvent event) {
        List<SseEmitter> deadEmitters = new java.util.ArrayList<>();
        AppConfig config = event.getPayload();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("config-update")
                        .data(config));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });

        // Remove dead emitters that failed to send the event
        emitters.removeAll(deadEmitters);
    }

    // Listen for CountdownTickEvent and broadcast the remaining time
    @EventListener
    public void onCountdownTick(com.mdz.deploystream.events.CountdownTickEvent event) {
        List<SseEmitter> deadEmitters = new java.util.ArrayList<>();
        int remaining = event.getRemainingSeconds();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("countdown-tick")
                        .data(java.util.Map.of("remaining", remaining)));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });

        emitters.removeAll(deadEmitters);
    }
}