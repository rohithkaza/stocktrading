package com.stockapp.stock_backend.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * FirebaseAuthFilter is a custom HTTP filter that authenticates requests using Firebase ID tokens.
 * Applied to secured endpoints to ensure only authenticated users can proceed.
 */
@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

    /**
     * Intercepts incoming HTTP requests to validate Firebase tokens.
     * If the token is valid, sets user ID and email as request attributes.
     * Rejects unauthorized requests with 401.
     *
     * @param request incoming HTTP request
     * @param response HTTP response to send if authentication fails
     * @param filterChain the filter chain to continue if authenticated
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        // Skip the filter for OPTIONS requests
        if (request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        // show request details for debugging
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request Method: " + request.getMethod());

        // Extract token from request body or cookies
        String token = extractTokenFromHeader(request);
        if (token == null) {
            System.err.println("Token not found in Header");
        } else {
            System.out.println("Token found in header!");
            System.out.println("Token: " + token);
        }

        if (token != null) {
            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                request.setAttribute("uid", decodedToken.getUid());
                request.setAttribute("email", decodedToken.getEmail());

            } catch (FirebaseAuthException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing authentication token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the Firebase token from the Authorization header.
     * @param request HTTP request containing the header
     * @return token string if present, otherwise null
     */
    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }


}