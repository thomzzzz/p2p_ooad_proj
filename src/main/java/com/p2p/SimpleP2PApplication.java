package com.p2p;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class SimpleP2PApplication {

    private static final Logger logger = LoggerFactory.getLogger(SimpleP2PApplication.class);

    public static void main(String[] args) {
        try {
            // Create upload directory if it doesn't exist
            File uploadDir = new File("./uploads");
            if (!uploadDir.exists()) {
                logger.info("Creating upload directory: {}", uploadDir.getAbsolutePath());
                boolean created = uploadDir.mkdirs();
                logger.info("Upload directory created: {}", created);
            } else {
                logger.info("Upload directory already exists: {}", uploadDir.getAbsolutePath());
            }
            
            // Check directory permissions
            Path uploadPath = Paths.get("./uploads");
            boolean canRead = Files.isReadable(uploadPath);
            boolean canWrite = Files.isWritable(uploadPath);
            logger.info("Upload directory permissions - Read: {}, Write: {}", canRead, canWrite);
            
            SpringApplication.run(SimpleP2PApplication.class, args);
        } catch (Exception e) {
            logger.error("Error during application startup", e);
            throw e;
        }
    }
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("*");
            }
        };
    }
}