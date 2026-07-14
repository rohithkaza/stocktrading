package com.stockapp.stock_backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Configures and registers a Firebase authentication filter for selected API endpoints.
 *  Ensures that only authenticated users can access protected routes.
 */

@Configuration
public class FilterConfig {

    @Autowired
    private FirebaseAuthFilter firebaseAuthFilter;

    /**
     * Registers the Firebase authentication filter for specific user and auth-related endpoints.
     * @return FilterRegistrationBean configured with secured routes
     */

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> firebaseAuthFilterRegistration() {

        String[] securedEndPoints = {
                "/auth/login",
                "/auth/signup",
                "/auth/google/handler",
                "/user/portfolioval",
                "/user/uninvested",
                "/user/stockshares",
                "/user/buy",
                "/user/sell",
                "/user/investments",
                "/user/portfolio/change",
                "/user/history",
                "/user/watchlist/get",
                "/user/watchlist/in",
                "/user/watchlist/add",
                "/user/watchlist/remove",
                "/user/industries",
                "/user/reset"
        };

        FilterRegistrationBean<OncePerRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(firebaseAuthFilter);
        registrationBean.addUrlPatterns(securedEndPoints); //"/user/balance", "/user/stockshares"
        registrationBean.addInitParameter("ignoreOptions", "true"); // Ignore OPTIONS requests
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
