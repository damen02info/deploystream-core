package com.mdz.deploystream.controllers;

import com.mdz.deploystream.entities.AppConfig;
import com.mdz.deploystream.services.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/all")
    public ResponseEntity<List<AppConfig>> getAllConfigs() {
        return ResponseEntity.ok(configService.getAllConfigs());
    }

}
