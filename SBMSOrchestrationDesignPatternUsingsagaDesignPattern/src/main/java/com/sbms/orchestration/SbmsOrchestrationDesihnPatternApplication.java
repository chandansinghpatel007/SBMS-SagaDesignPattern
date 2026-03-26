package com.sbms.orchestration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SbmsOrchestrationDesihnPatternApplication {

	public static void main(String[] args) {
		SpringApplication.run(SbmsOrchestrationDesihnPatternApplication.class, args);
	}
}
