package com.project.libmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.project.libmanager", "com.project.libmanager.mapper" })
public class LibManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibManagerApplication.class, args);
	}

}
