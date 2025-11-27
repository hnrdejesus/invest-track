package com.github.hnrdejesus.invest_track.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for frontend integration.
 * Allows Angular application to make requests to backend API.
 */
@Configuration
public class CorsConfig {

    /**
     * Configures CORS filter to allow cross-origin requests.
     * In production, restrict allowed origins to specific domains.
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        // Allowed origins (frontend URLs)
        // In production, replace with actual frontend domain
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",     // Angular dev server
                "http://localhost:3000"      // Alternative port
        ));

        // Allowed HTTP methods
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Allowed headers
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"
        ));

        // Exposed headers (visible to frontend)
        config.setExposedHeaders(List.of(
                "Authorization",
                "Content-Disposition"
        ));

        // Preflight cache duration (seconds)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}