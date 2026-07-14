package com.stockapp.stock_backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures security settings for the application.
 * Disables CSRF protection, allows all requests, and applies custom CORS configuration.
 */

@Configuration
public class SecurityConfig {

    @Autowired
    CustomCorsConfiguration customCorsConfiguration;

    /**
     * Configures the application's security filter chain.
     * - Disables CSRF protection
     * - Permits all HTTP requests
     * - Applies custom CORS configuration
     *
     * @param httpSecurity the HttpSecurity object used to configure security
     * @return the configured SecurityFilterChain
     * @throws Exception if security configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req.anyRequest().permitAll())
                .cors(c -> c.configurationSource(customCorsConfiguration));
        return httpSecurity.build();
    }
}
