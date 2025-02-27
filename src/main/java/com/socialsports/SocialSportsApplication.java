package com.socialsports;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SocialSportsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialSportsApplication.class, args);
    }
}