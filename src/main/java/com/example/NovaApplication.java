package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NovaApplication {
	private boolean migrationCompleted;


    public static void main(String[] args) {
		SpringApplication.run(NovaApplication.class, args);
	}


}
