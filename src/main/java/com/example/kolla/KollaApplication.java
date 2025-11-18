package com.example.kolla;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.logging.Logger;

@SpringBootApplication
@EnableScheduling
public class KollaApplication {
	private static final Logger logger = Logger.getLogger(KollaApplication.class.getName());

	public static void main(String[] args) {
		try {
			// Try to load .env file, but don't fail if it doesn't exist
			Dotenv dotenv = Dotenv.configure()
					.directory(".")
					.filename(".env")
					.ignoreIfMissing() // This makes .env file optional
					.load();

			// Set system properties from .env file
			dotenv.entries().forEach(entry -> {
				System.setProperty(entry.getKey(), entry.getValue());
			});
			
			logger.info("Loaded environment variables from .env file");
		} catch (Exception e) {
			// Log warning but continue - environment variables can be set via system properties or other means
			logger.warning("Could not load .env file: " + e.getMessage() + ". Continuing with system properties...");
		}

		SpringApplication.run(KollaApplication.class, args);
	}
}
