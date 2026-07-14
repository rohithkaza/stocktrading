package com.stockapp.stock_backend.controller;

import com.stockapp.stock_backend.server.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles authentication-related API endpoints.
 * Supports user login, signup, and Google account handling using Firebase.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    /**
     * Logs in a user based on Firebase-provided UID and email.
     * @param uid Firebase user ID
     * @param email Firebase user email
     * @return ResponseEntity with login status
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestAttribute("uid") String uid,
                                        @RequestAttribute("email") String email) {
        return authService.loginUser(uid, email);
    }
    /**
     * Handles Google Sign-In authentication and account linking.
     * @param uid Firebase user ID
     * @param email Firebase user email
     * @return ResponseEntity with status of the Google auth process
     */

    @PostMapping("/google/handler")
    public ResponseEntity<String> googleAuthHandler(@RequestAttribute("uid") String uid,
                                         @RequestAttribute("email") String email) {
        return authService.handleGoogle(uid, email);
    }
    /**
     * Signs up a new user using Firebase UID and email.
     * @param uid Firebase user ID
     * @param email Firebase user email
     * @return ResponseEntity with signup confirmation
     */

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestAttribute("uid") String uid,
                                         @RequestAttribute("email") String email) {
        return authService.signupUser(uid, email);
    }

}
