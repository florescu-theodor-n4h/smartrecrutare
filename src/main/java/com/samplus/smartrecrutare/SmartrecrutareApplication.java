package com.samplus.smartrecrutare;

import org.jspecify.annotations.NullMarked;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Pentru packaging WAR, trebuie extinsa clasa SpringBootServletInitializer
 */
@SpringBootApplication
public class SmartrecrutareApplication extends SpringBootServletInitializer {

	@NullMarked
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SmartrecrutareApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SmartrecrutareApplication.class, args);
	}
}