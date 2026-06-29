package com.mdz.deploystream.services;


import com.mdz.deploystream.entities.AppConfig;
import com.mdz.deploystream.events.AppConfigUpdateEvent;
import com.mdz.deploystream.repositories.AppConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
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
        AppConfig config = appConfigRepository.findById(key)
                .orElse(AppConfig.builder().configKey(key).build());

        config.setConfigValue(value);
        config.setLastModified(LocalDateTime.now());

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

}
