package com.mdz.deploystream.controllers;

import com.mdz.deploystream.entities.AppConfig;
import com.mdz.deploystream.services.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    @GetMapping("/{key}")
    public ResponseEntity<AppConfig> getConfigByKey(@PathVariable String key) {
        return configService.getConfig(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/lock")
    public ResponseEntity<Boolean> isDeploymentLocked() {
        return ResponseEntity.ok(configService.isSystemLocked());
    }

}
