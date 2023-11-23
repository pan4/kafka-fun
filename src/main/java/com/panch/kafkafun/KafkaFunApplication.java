package com.panch.kafkafun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class KafkaFunApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaFunApplication.class, args);
	}

}
