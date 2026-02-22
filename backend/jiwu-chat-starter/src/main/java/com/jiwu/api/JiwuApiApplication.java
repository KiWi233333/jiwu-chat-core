package com.jiwu.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class JiwuApiApplication {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        SpringApplication.run(JiwuApiApplication.class, args);
        log.info("启动耗时：{}ms", System.currentTimeMillis() - startTime);
    }

}
