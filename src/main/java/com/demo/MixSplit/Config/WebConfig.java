package com.demo.MixSplit.Config;
// Step 1: Import necessary classes
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Step 2: Add @Configuration annotation to mark this class as a configuration class
@Configuration
public class WebConfig {
    // Step 3: Create a WebMvcConfigurer bean to configure CORS globally
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Step 4: Define the paths that require CORS (e.g., all APIs starting with "/api/")
                registry.addMapping("/api/**")
                        // Step 5: Specify the allowed origins (e.g., your frontend running on localhost:3000)
                        .allowedOrigins("http://localhost:3000")
                        // Step 6: Specify the allowed HTTP methods
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        // Step 7: Allow any headers to be sent (optional, can be restricted if needed)
                        .allowedHeaders("*")
                        // Step 8: Allow credentials (optional if you need cookie-based authentication)
                        .allowCredentials(true);
            }
        };
    }
}
