package com.finalproject.Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TravelApplication {
    public static void main(String[] args) {
      
        SpringApplication.run(TravelApplication.class, args);
    }
}