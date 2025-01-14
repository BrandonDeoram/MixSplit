package com.demo.MixSplit;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRabbit
@EnableScheduling
public class MixSplitApplication {
	public static void main(String[] args) {
		SpringApplication.run(MixSplitApplication.class, args);
	}
}