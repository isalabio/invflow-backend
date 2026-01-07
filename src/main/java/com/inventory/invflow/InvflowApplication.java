package com.inventory.invflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class InvflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvflowApplication.class, args);
	}

}
