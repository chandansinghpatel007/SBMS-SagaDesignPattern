package com.sbms.busbookingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SbmsBusBookingUsingSagaDesignPatternApplication {

	public static void main(String[] args) {
		SpringApplication.run(SbmsBusBookingUsingSagaDesignPatternApplication.class, args);
	}

}
