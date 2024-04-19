package com.com.jeoluspug;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableBatchProcessing
@SpringBootApplication
public class parallelBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(parallelBatchApplication.class, args);
    }
}
