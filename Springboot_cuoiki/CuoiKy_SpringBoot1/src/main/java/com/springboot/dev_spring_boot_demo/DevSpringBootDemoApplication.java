package com.springboot.dev_spring_boot_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.springboot.dev_spring_boot_demo"})
public class DevSpringBootDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(DevSpringBootDemoApplication.class, args);
	}
}
