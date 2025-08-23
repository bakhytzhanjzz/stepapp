package com.stepapp;

import org.springframework.boot.SpringApplication;

public class TestStepappApplication {

	public static void main(String[] args) {
		SpringApplication.from(StepappApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
