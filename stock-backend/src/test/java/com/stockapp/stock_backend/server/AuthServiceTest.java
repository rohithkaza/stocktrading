package com.stockapp.stock_backend.server;

import com.stockapp.stock_backend.model.User;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private final AuthService authService = new AuthService();

    /**
     * Test loginUser when user exists
     */
    @Test
    void testLoginUser_userExists() {
        try (MockedStatic<User> mockedUser = Mockito.mockStatic(User.class)) {
            mockedUser.when(() -> User.userExists("123")).thenReturn(true);
            ResponseEntity<String> response = authService.loginUser("123", "test@example.com");
            assertEquals(200, response.getStatusCodeValue());
            assertTrue(response.getBody().contains("logged in successfully"));
        }
    }

    /**
     * Test loginUser when user does not exist
     */
    @Test
    void testLoginUser_userNotFound() {
        try (MockedStatic<User> mockedUser = Mockito.mockStatic(User.class)) {
            mockedUser.when(() -> User.userExists("404")).thenReturn(false);
            ResponseEntity<String> response = authService.loginUser("404", "notexist@example.com");
            assertEquals(500, response.getStatusCodeValue());
            assertEquals("Unable to Find User", response.getBody());
        }
    }

    /**
     * Test signupUser when creation is successful.
     */
    @Test
    void testSignupUser_success() {
        try (MockedStatic<User> mockedUser = Mockito.mockStatic(User.class)) {
            mockedUser.when(() -> User.createUser("789", "new@example.com", "new@example.com")).then(inv -> null);
            mockedUser.when(() -> User.userExists("789")).thenReturn(true);
            ResponseEntity<String> response = authService.signupUser("789", "new@example.com");
            assertEquals(200, response.getStatusCodeValue());
            assertTrue(response.getBody().contains("Successfully created"));
        }
    }

    /**
     * Test signupUser when user creation fails.
     */
    @Test
    void testSignupUser_failure() {
        try (MockedStatic<User> mockedUser = Mockito.mockStatic(User.class)) {
            mockedUser.when(() -> User.createUser("fail", "fail@example.com", "fail@example.com")).then(inv -> null);
            mockedUser.when(() -> User.userExists("fail")).thenReturn(false);
            ResponseEntity<String> response = authService.signupUser("fail", "fail@example.com");
            assertEquals(500, response.getStatusCodeValue());
            assertEquals("Unable to Create User", response.getBody());
        }
    }

    /**
     * Test handleGoogle when user already exists.
     */
    @Test
    void testHandleGoogle_existingUser() {
        try (MockedStatic<User> mockedUser = Mockito.mockStatic(User.class)) {
            mockedUser.when(() -> User.userExists("g1")).thenReturn(true);
            ResponseEntity<String> response = authService.handleGoogle("g1", "google@example.com");
            assertEquals(200, response.getStatusCodeValue());
            assertTrue(response.getBody().contains("logged in successfully"));
        }
    }

    /**
     * Test handleGoogle when user does not exist (sign up).
     */
    @Test
    void testHandleGoogle_newUser() {
        try (MockedStatic<User> mockedUser = Mockito.mockStatic(User.class)) {
            mockedUser.when(() -> User.userExists("g2")).thenReturn(false).thenReturn(true);
            mockedUser.when(() -> User.createUser("g2", "google2@example.com", "google2@example.com")).then(inv -> null);
            ResponseEntity<String> response = authService.handleGoogle("g2", "google2@example.com");
            assertEquals(200, response.getStatusCodeValue());
            assertTrue(response.getBody().contains("Successfully created"));
        }
    }
}
