package com.guardianvision.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.guardianvision.backend.repository")
public class GuardianVisionApplication {

	public static void main(String[] args) {
		SpringApplication.run(GuardianVisionApplication.class, args);
	}

}
