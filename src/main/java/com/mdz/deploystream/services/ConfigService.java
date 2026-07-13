package com.mdz.deploystream.services;


import com.mdz.deploystream.entities.AppConfig;
import com.mdz.deploystream.events.AppConfigUpdateEvent;
import com.mdz.deploystream.repositories.AppConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConfigService {

    private final AppConfigRepository appConfigRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Transactional(readOnly = true)
    public Optional<AppConfig> getConfig(String key) {
        return appConfigRepository.findById(key);
    }

    @Transactional
    public AppConfig updateConfig(String key, String value) {
        AppConfig config = appConfigRepository.findById(key).orElse(AppConfig.builder().configKey(key).configValue(value).isLocked(false).lastModified(LocalDateTime.now()).build());
        config.setConfigValue(value);
        config.setLastModified(LocalDateTime.now());
        if (config.getIsLocked() == null) {
            config.setIsLocked(false);
        }
        AppConfig savedConfig = appConfigRepository.save(config);
        eventPublisher.publishEvent(new AppConfigUpdateEvent(this, savedConfig));
        return savedConfig;
    }

    @Transactional
    public void setSystemLock(boolean isLocked) {
        AppConfig lockConfig = appConfigRepository.findById("SYSTEM_LOCK")
                .orElse(AppConfig.builder().configKey("SYSTEM_LOCK").configValue("false").build());
        lockConfig.setIsLocked(isLocked);
        lockConfig.setLastModified(LocalDateTime.now());

        AppConfig savedConfig = appConfigRepository.save(lockConfig);
        eventPublisher.publishEvent(new AppConfigUpdateEvent(this, savedConfig));
    }

    @Transactional(readOnly = true)
    public boolean isSystemLocked() {
        return appConfigRepository.findById("SYSTEM_LOCK")
                .map(AppConfig::getIsLocked)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<AppConfig> getAllConfigs() {
        return appConfigRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AppConfig emitConfigUpdate(String key, String value) {
        AppConfig payload = AppConfig.builder()
                .configKey(key)
                .configValue(value)
                .lastModified(LocalDateTime.now())
                .isLocked(Boolean.FALSE)
                .build();

        eventPublisher.publishEvent(new AppConfigUpdateEvent(this, payload));
        return payload;
    }

    @Async("asyncExecutor")
    public void startAutomaticRollbackCountdown(String defaultColor) {
        AppConfig lock = appConfigRepository.findById("SYSTEM_LOCK").orElse(null);
        if (lock == null || !Boolean.TRUE.equals(lock.getIsLocked()) || lock.getLastModified() == null) {
            return;
        }

        log.info("Rollback countdown started");
        log.info("SYSTEM_LOCK = {}, lastModified = {}", lock.getIsLocked(), lock.getLastModified());
        LocalDateTime expireAt = lock.getLastModified().plusSeconds(26);

        while (LocalDateTime.now().isBefore(expireAt)) {
            if (!isSystemLocked()) {
                log.info("Countdown aborted: System was unlocked manually.");
                return;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Rollback thread interrupted", e);
                Thread.currentThread().interrupt();
                return;
            }
        }

        if (!isSystemLocked()) {
            return;
        }

        updateConfig("COLOR", defaultColor);
        log.info("Updated COLOR to default value: {}", defaultColor);
        setSystemLock(false);
    }
}
