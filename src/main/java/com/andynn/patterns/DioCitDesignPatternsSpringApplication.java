package com.andynn.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class DioCitDesignPatternsSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(DioCitDesignPatternsSpringApplication.class, args);
	}

}
