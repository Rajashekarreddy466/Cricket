package com.cricket.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.cricket.controller", "com.cricket.service"})
public class CricketTopElevenApplication {

	public static void main(String[] args) {
		SpringApplication.run(CricketTopElevenApplication.class, args);
	}

}
