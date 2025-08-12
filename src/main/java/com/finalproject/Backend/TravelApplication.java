package com.finalproject.Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.*;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TravelApplication {
    public static void main(String[] args) {
        try {
            // Try to load .env file if it exists (development environment)
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            
            // Only set properties from .env if they're not already set as environment variables
            if (System.getenv("DATABASE_URL") == null && dotenv.get("DATABASE_URL") != null) {
                System.setProperty("DATABASE_URL", dotenv.get("DATABASE_URL"));
            }
            if (System.getenv("DATABASE_USERNAME") == null && dotenv.get("DATABASE_USERNAME") != null) {
                System.setProperty("DATABASE_USERNAME", dotenv.get("DATABASE_USERNAME"));
            }
            if (System.getenv("DATABASE_PASSWORD") == null && dotenv.get("DATABASE_PASSWORD") != null) {
                System.setProperty("DATABASE_PASSWORD", dotenv.get("DATABASE_PASSWORD"));
            }
            if (System.getenv("FILE_UPLOAD_DIRECTORY") == null && dotenv.get("FILE_UPLOAD_DIRECTORY") != null) {
                System.setProperty("FILE_UPLOAD_DIRECTORY", dotenv.get("FILE_UPLOAD_DIRECTORY"));
            }
            if (System.getenv("SECRECT_JWT_KEY") == null && dotenv.get("SECRECT_JWT_KEY") != null) {
                System.setProperty("SECRECT_JWT_KEY", dotenv.get("SECRECT_JWT_KEY"));
            }
        } catch (Exception e) {
            // Log the error but continue with application startup
            System.out.println("Warning: Could not load .env file. Using system environment variables.");
        }
        
        SpringApplication.run(TravelApplication.class, args);
    }
}
