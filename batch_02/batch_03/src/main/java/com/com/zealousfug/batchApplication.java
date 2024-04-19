// DB에서 DB로 batch

package com.com.zealousfug;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableBatchProcessing
@SpringBootApplication
public class batchApplication {

    public static void main(String[] args) {
        SpringApplication.run(batchApplication.class, args);
    }
}
