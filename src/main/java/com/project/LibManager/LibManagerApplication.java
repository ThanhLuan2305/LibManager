package com.project.LibManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.project.LibManager", "com.project.LibManager.mapper"})
public class LibManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibManagerApplication.class, args);
	}

}
