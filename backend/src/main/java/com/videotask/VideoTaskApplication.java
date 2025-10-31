package com.videotask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VideoTaskApplication {
    public static void main(String[] args) {
        SpringApplication.run(VideoTaskApplication.class, args);
    }




} 