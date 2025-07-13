package com.guardianvision.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.guardianvision.backend.repository")
@EnableAsync
public class GuardianVisionApplication {

	public static void main(String[] args) {
		SpringApplication.run(GuardianVisionApplication.class, args);
	}

}
