package com.example.pucpvalidadorservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PucpValidadorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PucpValidadorServiceApplication.class, args);
    }

}
