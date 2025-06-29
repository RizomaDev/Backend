package com.finalproject.Backend;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.*;

@SpringBootApplication
public class TravelApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
		System.setProperty("DATABASE_URL", dotenv.get("DATABASE_URL"));
		System.setProperty("DATABASE_USERNAME", dotenv.get("DATABASE_USERNAME"));
		System.setProperty("DATABASE_PASSWORD", dotenv.get("DATABASE_PASSWORD"));
		System.setProperty("FILE_UPLOAD_DIRECTORY", dotenv.get("FILE_UPLOAD_DIRECTORY"));
		System.setProperty("SECRECT_JWT_KEY", dotenv.get("SECRECT_JWT_KEY"));
        SpringApplication.run(TravelApplication.class, args);
    }
}
