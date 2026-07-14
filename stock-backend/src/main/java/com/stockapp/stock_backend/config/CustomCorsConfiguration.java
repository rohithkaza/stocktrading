package com.stockapp.stock_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

/** Handles Cross-Origin Resource Sharing (CORS) settings for incoming HTTP requests.
 * Allows frontend apps hosted on specific origins to interact with the backend.
 */

@Component
public class CustomCorsConfiguration implements CorsConfigurationSource {

    /**
     * Configures allowed origins, HTTP methods, and headers for cross-origin requests
     * @return CorsConfiguration allowing specific frontend domains
     */
    @Override
    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "https://comp-413-group-a-stocks.vercel.app"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        return config;
    }
}
