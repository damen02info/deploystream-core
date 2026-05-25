package com.mdz.deploystream.tasks;

import com.mdz.deploystream.repositories.AppConfigRepository;
import com.mdz.deploystream.services.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RollbackTask {

    private final AppConfigRepository appConfigRepository;
    private final ConfigService configService;

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void executeAutomaticRollbackCheck() {
        // Verify if the system lock is active and has been active for more than 5 minutes
        appConfigRepository.findById("SYSTEM_LOCK").ifPresent(lockConfig -> {
            if (Boolean.TRUE.equals(lockConfig.getIsLocked())) {
                LocalDateTime lastModified = lockConfig.getLastModified();
                LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(5);

                if (lastModified != null && lastModified.isBefore(thresholdTime)) {
                    log.warn("CRÍTICO: Se ha detectado un bloqueo del sistema que ha expirado (más de 5 minutos activo desde {}). Iniciando rollback automático...", lastModified);
                }

                // Perform rollback actions on the theme color and unlock the system
                appConfigRepository.findById("THEME_COLOR").ifPresent(themeConfig -> {
                    themeConfig.setConfigValue("#DEFAULT_COLOR"); // TODO change on prod
                    themeConfig.setLastModified(LocalDateTime.now());
                    appConfigRepository.save(themeConfig);
                    log.info("RollbackTask: Color de la interfaz restaurado al valor por defecto.");
                });
                configService.setSystemLock(false);
                log.info("RollbackTask: El bloqueo del sistema ha sido levantado con éxito de forma autónoma.");
            }
        });
    }
}
