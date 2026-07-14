package com.stockapp.stock_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Provides a BCryptPasswordEncoder bean for securely hashing user passwords.
 * This encoder can be injected wherever password encoding or verification is needed.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Creates and returns a BCryptPasswordEncoder instance.
     * @return BCryptPasswordEncoder for password hashing
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}