package com.stockapp.stock_backend.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.stockapp.stock_backend.server.ServerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServerService serverService;

    private final String uid = "testUid";

    @BeforeEach
    void setUp() throws ExecutionException, InterruptedException {
        JsonObject json = new JsonObject();
        json.addProperty("key", "value");

        JsonArray jsonArray = new JsonArray();
        jsonArray.add(json);

        Mockito.when(serverService.signup(anyString(), anyString(), anyString()))
                .thenReturn(ResponseEntity.ok("signed up"));
        Mockito.when(serverService.getPortfolioValue(anyString()))
                .thenReturn(ResponseEntity.ok(json));
        Mockito.when(serverService.getUninvested(anyString()))
                .thenReturn(ResponseEntity.ok(json));
        Mockito.when(serverService.getStockShares(anyString(), anyString()))
                .thenReturn(ResponseEntity.ok(json));
        Mockito.when(serverService.buyStock(anyString(), anyString(), anyInt()))
                .thenReturn(ResponseEntity.ok("buy success"));
        Mockito.when(serverService.sellStock(anyString(), anyString(), anyInt()))
                .thenReturn(ResponseEntity.ok("sell success"));
        Mockito.when(serverService.getInvestments(anyString()))
                .thenReturn(ResponseEntity.ok("investment data"));
        Mockito.when(serverService.getPortfolioChange(anyString()))
                .thenReturn(ResponseEntity.ok(Map.of("change", 3.25)));
        Mockito.when(serverService.getTransactionHistory(anyString()))
                .thenReturn(ResponseEntity.ok("tx history"));
        Mockito.when(serverService.getWatchlist(anyString()))
                .thenReturn(ResponseEntity.ok(jsonArray));
        Mockito.when(serverService.inWatchlist(anyString(), anyString()))
                .thenReturn(ResponseEntity.ok(true));
        Mockito.when(serverService.addToWatchlist(anyString(), anyString()))
                .thenReturn(ResponseEntity.ok("added"));
        Mockito.when(serverService.removeFromWatchlist(anyString(), anyString()))
                .thenReturn(ResponseEntity.ok("removed"));
        Mockito.when(serverService.getIndustryBreakdown(anyString()))
                .thenReturn(ResponseEntity.ok(json));
        Mockito.when(serverService.resetAccount(anyString()))
                .thenReturn(ResponseEntity.ok("reset done"));
    }

    @Test
    void testSignup() throws Exception {
        mockMvc.perform(post("/user/signup")
                        .requestAttr("uid", uid)
                        .requestAttr("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("signed up"));
    }

    @Test
    void testGetPortfolioValue() throws Exception {
        mockMvc.perform(post("/user/portfolioval")
                        .requestAttr("uid", uid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("value"));
    }

    @Test
    void testBuy() throws Exception {
        mockMvc.perform(post("/user/buy")
                        .requestAttr("uid", uid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockSymbol\":\"AAPL\",\"quantity\":\"10\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("buy success"));
    }

    @Test
    void testSell() throws Exception {
        mockMvc.perform(post("/user/sell")
                        .requestAttr("uid", uid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockSymbol\":\"AAPL\",\"quantity\":\"5\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("sell success"));
    }

    @Test
    void testGetPortfolioChange() throws Exception {
        mockMvc.perform(post("/user/portfolio/change")
                        .requestAttr("uid", uid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.change").value(3.25));
    }

    @Test
    void testGetWatchlist() throws Exception {
        mockMvc.perform(post("/user/watchlist/get")
                        .requestAttr("uid", uid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testInWatchlist() throws Exception {
        mockMvc.perform(post("/user/watchlist/in")
                        .requestAttr("uid", uid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockSymbol\":\"AAPL\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testResetAccount() throws Exception {
        mockMvc.perform(post("/user/reset")
                        .requestAttr("uid", uid))
                .andExpect(status().isOk())
                .andExpect(content().string("reset done"));
    }
}
