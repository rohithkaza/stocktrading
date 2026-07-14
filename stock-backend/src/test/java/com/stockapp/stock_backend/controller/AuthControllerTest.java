package com.stockapp.stock_backend.controller;

import com.stockapp.stock_backend.server.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)  // disables security filters (o.w. get 403 Forbidden Error)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    private final String uid = "testUid";
    private final String email = "test@example.com";

    @BeforeEach
    void setUp() {
        Mockito.when(authService.loginUser(eq(uid), eq(email)))
                .thenReturn(ResponseEntity.ok("Login successful"));

        Mockito.when(authService.signupUser(eq(uid), eq(email)))
                .thenReturn(ResponseEntity.ok("Signup successful"));

        Mockito.when(authService.handleGoogle(eq(uid), eq(email)))
                .thenReturn(ResponseEntity.ok("Google auth handled"));
    }

    @Test
    void testLogin() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .requestAttr("uid", uid)
                        .requestAttr("email", email))
                .andExpect(status().isOk())
                .andExpect(content().string("Login successful"));
    }

    @Test
    void testSignup() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .requestAttr("uid", uid)
                        .requestAttr("email", email))
                .andExpect(status().isOk())
                .andExpect(content().string("Signup successful"));
    }

    @Test
    void testGoogleAuthHandler() throws Exception {
        mockMvc.perform(post("/auth/google/handler")
                        .requestAttr("uid", uid)
                        .requestAttr("email", email))
                .andExpect(status().isOk())
                .andExpect(content().string("Google auth handled"));
    }
}
