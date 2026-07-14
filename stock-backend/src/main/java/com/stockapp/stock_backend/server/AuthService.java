package com.stockapp.stock_backend.server;

import com.stockapp.stock_backend.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

/**
 * AuthService handles user authentication.
 * It provides methods for user login, signup, and Google authentication handling.
 */
@Service
@RestController
public class AuthService {

    /**
     * Logs in a user based on UID and email.
     *
     * @param uid   The unique identifier of the user.
     * @param email The email address of the user.
     * @return ResponseEntity with the status of the login operation.
     */
    public ResponseEntity<String> loginUser(String uid, String email) {

        boolean userExists = User.userExists(uid);

        if (!userExists) {
            // User not found
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to Find User");
        } else {
            String response = "User with email: " + email + " logged in successfully.";

            return ResponseEntity.ok().body(response);
        }
    }

    /**
     * Sign up a new user with the given UID and email.
     *
     * @param uid   The unique identifier of the user.
     * @param email The email address of the user.
     * @return ResponseEntity with the status of the signup operation.
     */
    public ResponseEntity<String> signupUser(String uid, String email) {
        // Create a new user with the given UID and username
        User.createUser(uid, email, email);
        boolean userCreated = User.userExists(uid);

        if (!userCreated) {
            // User not found
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to Create User");
        } else {
            String response = "Successfully created user with email: " + email;
            return ResponseEntity.ok().body(response);
        }
    }

    /**
     * Handles Google Sign-In authentication and account linking.
     * Checks if the user exists, logs them in or signs them up accordingly.
     *
     * @param uid   Firebase user ID
     * @param email Firebase user email
     * @return ResponseEntity with status of the Google auth process
     */
    public ResponseEntity<String> handleGoogle(String uid, String email) {
        if (User.userExists(uid)) {
            return loginUser(uid, email);
        } else {
            return signupUser(uid, email);
        }
    }
}
