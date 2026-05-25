package com.mdz.deploystream.services;


import com.mdz.deploystream.entities.AppConfig;
import com.mdz.deploystream.repositories.AppConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final AppConfigRepository appConfigRepository;

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

        return appConfigRepository.save(config);
    }

    @Transactional
    public void setSystemLock(boolean isLocked) {
        AppConfig lockConfig = appConfigRepository.findById("SYSTEM_LOCK")
                .orElse(AppConfig.builder().configValue("false").build());

        lockConfig.setIsLocked(isLocked);
        lockConfig.setLastModified(LocalDateTime.now());
        appConfigRepository.save(lockConfig);
    }

    @Transactional(readOnly = true)
    public boolean isSystemLocked() {
        return appConfigRepository.findById("SYSTEM_LOCK")
                .map(AppConfig::getIsLocked)
                .orElse(false);
    }

}
