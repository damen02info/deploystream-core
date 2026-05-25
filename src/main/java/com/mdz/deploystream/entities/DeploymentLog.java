package com.mdz.deploystream.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "DEPLOYMENT_LOGS")
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Builder
public class DeploymentLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOG_ID")
    private Long logId;

    @Column(name = "DEPLOYMENT_ID", nullable = false, length = 50)
    private String deploymentId;

    @Column(name = "LOG_TIMESTAMP", nullable = false)
    private LocalDateTime logTimestamp;

    @Column(name = "LOG_LEVEL", nullable = false, length = 10)
    private String logLevel;

    @Column(name = "MESSAGE", nullable = false, length = 4000)
    private String message;
}
