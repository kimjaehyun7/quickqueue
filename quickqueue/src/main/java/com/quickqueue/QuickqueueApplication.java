package com.quickqueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class QuickqueueApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuickqueueApplication.class, args);
	}

}
