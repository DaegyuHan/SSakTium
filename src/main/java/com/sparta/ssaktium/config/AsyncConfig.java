package com.sparta.ssaktium.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);   // 기본 스레드 수
        executor.setMaxPoolSize(20);   // 최대 스레드 수
        executor.setQueueCapacity(100); // 대기 큐 크기
        executor.setThreadNamePrefix("EmailThread-");
        executor.initialize();
        logThreadPoolStatus(executor, "EmailTaskExecutor");
        return executor;
    }

    @Bean(name = "couponBatchExecutor")
    public Executor couponBatchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);   // 동시에 실행할 기본 스레드 수
        executor.setMaxPoolSize(8);    // 최대 스레드 수
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("CouponBatch-");
        executor.initialize();
        logThreadPoolStatus(executor, "CouponBatchExecutor");
        return executor;
    }

    private void logThreadPoolStatus(ThreadPoolTaskExecutor executor, String name) {
        System.out.println("[" + name + "] Active Threads: " + executor.getActiveCount());
        System.out.println("[" + name + "] Total Tasks: " + executor.getThreadPoolExecutor().getTaskCount());
        System.out.println("[" + name + "] Completed Tasks: " + executor.getThreadPoolExecutor().getCompletedTaskCount());
    }
}
