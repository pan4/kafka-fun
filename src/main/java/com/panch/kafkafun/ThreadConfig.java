package com.panch.kafkafun;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadConfig {

    @Bean
    public ThreadPoolTaskExecutor vtaExecutor() {
        ThreadPoolTaskExecutor pool = createThreadPoolTaskExecutor(1, 20);
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return pool;
    }

    private ThreadPoolTaskExecutor createThreadPoolTaskExecutor(int poolCore, int poolMax) {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setQueueCapacity(0);
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        pool.setCorePoolSize(poolCore);
        pool.setMaxPoolSize(poolMax);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }
}
