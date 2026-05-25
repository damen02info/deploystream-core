package com.mdz.deploystream.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "APP_CONFIG")
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Builder
public class AppConfig {

    @Id
    @Column(name = "CONFIG_KEY")
    private String configKey;

    @Column(name = "CONFIG_VALUE", nullable = false)
    private String configValue;

    @Column(name = "LAST_MODIFIED", nullable = false)
    private LocalDateTime lastModified;

    @Column(name = "IS_LOCKED", nullable = false)
    private Boolean isLocked;
}
