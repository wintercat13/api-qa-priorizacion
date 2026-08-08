package com.qapriorizacion.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiQaPriorizacionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiQaPriorizacionApplication.class, args);
    }

}
