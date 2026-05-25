package com.mdz.deploystream.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // Activate Spring asynchronous processing
public class AsyncConfig {

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Configure the thread pool parameters
        executor.setCorePoolSize(4);

        // Set the maximum number of threads in the pool
        executor.setMaxPoolSize(10);

        // Set the capacity of the queue for tasks before they are executed
        executor.setQueueCapacity(50);

        // Set the prefix for the names of the threads in the pool
        executor.setThreadNamePrefix("JenkinsAsync-");

        executor.initialize();

        return executor;
    }
}
