package com.stockapp.stock_backend.server;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.cloud.firestore.Firestore;
import com.google.gson.JsonObject;
import com.stockapp.stock_backend.model.Stock;
import com.stockapp.stock_backend.model.User;
import java.io.IOException;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


class ServerServiceTest {

    private Firestore mockFirestore;
    private BCryptPasswordEncoder mockEncoder;
    private ServerService service;

    @BeforeEach
    void setUp() {
        mockFirestore = mock(Firestore.class, RETURNS_DEEP_STUBS); // deep stubs for any chained calls
        mockEncoder   = mock(BCryptPasswordEncoder.class);
        service       = new ServerService(mockFirestore, mockEncoder);
    }

    @Test
    void helloWorld_returns200AndMessage() {
        ResponseEntity<String> resp = service.helloWorld();
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("Hello World!", resp.getBody());
    }


    @Test
    void buyStock_success() throws Exception {
        try (MockedStatic<Stock> stock = Mockito.mockStatic(Stock.class);
             MockedStatic<User>  user  = Mockito.mockStatic(User.class)) {
            stock.when(() -> Stock.getCurrentPrice("AAPL")).thenReturn(100.0f); // returns float
            user.when(()  -> User.buyStock("uid", "AAPL", 5, 100.0)).thenReturn(true);
            ResponseEntity<String> resp = service.buyStock("uid", "AAPL", 5);
            assertEquals(200, resp.getStatusCodeValue());
            assertEquals("Stock bought successfully", resp.getBody());
        }
    }

    @Test
    void buyStock_insufficientFunds() throws Exception {
        try (MockedStatic<Stock> stock = Mockito.mockStatic(Stock.class);
             MockedStatic<User>  user  = Mockito.mockStatic(User.class)) {
            stock.when(() -> Stock.getCurrentPrice("AAPL")).thenReturn(100.0f);
            user.when(()  -> User.buyStock("uid", "AAPL", 5, 100.0)).thenReturn(false);
            ResponseEntity<String> resp = service.buyStock("uid", "AAPL", 5);
            assertEquals(400, resp.getStatusCodeValue());
            assertEquals("Insufficient funds to buy stock", resp.getBody());
        }
    }


    @Test
    void sellStock_success() throws Exception {
        try (MockedStatic<Stock> stock = Mockito.mockStatic(Stock.class);
             MockedStatic<User>  user  = Mockito.mockStatic(User.class)) {
            stock.when(() -> Stock.getCurrentPrice("TSLA")).thenReturn(250.0f);
            user.when(()  -> User.sellStock("uid", "TSLA", 2, 250.0)).thenReturn(true);
            ResponseEntity<String> resp = service.sellStock("uid", "TSLA", 2);
            assertEquals(200, resp.getStatusCodeValue());
            assertEquals("Stock sold successfully", resp.getBody());
        }
    }

    @Test
    void sellStock_notEnoughShares() throws Exception {
        try (MockedStatic<Stock> stock = Mockito.mockStatic(Stock.class);
             MockedStatic<User>  user  = Mockito.mockStatic(User.class)) {
            stock.when(() -> Stock.getCurrentPrice("TSLA")).thenReturn(250.0f);
            user.when(()  -> User.sellStock("uid", "TSLA", 2, 250.0)).thenReturn(false);
            ResponseEntity<String> resp = service.sellStock("uid", "TSLA", 2);
            assertEquals(400, resp.getStatusCodeValue());
            assertEquals("Not enough shares to sell", resp.getBody());
        }
    }

    @Test
    void getCurrentPrice_success() throws Exception {
        try (MockedStatic<Stock> stock = Mockito.mockStatic(Stock.class)) {
            stock.when(() -> Stock.getCurrentPrice("MSFT")).thenReturn(333.25f);
            ResponseEntity<JsonObject> resp = service.getCurrentPrice("MSFT");
            assertEquals(200, resp.getStatusCodeValue());
            assertEquals(333.25f, resp.getBody().get("price").getAsFloat());
        }
    }

    @Test
    void getCurrentPrice_apiErrorReturns400() throws Exception {
        try (MockedStatic<Stock> stock = Mockito.mockStatic(Stock.class)) {
            stock.when(() -> Stock.getCurrentPrice("FAIL")).thenThrow(new IOException("API Down"));
            ResponseEntity<JsonObject> resp = service.getCurrentPrice("FAIL");
            assertEquals(400, resp.getStatusCodeValue());
            assertEquals("API Down", resp.getBody().get("message").getAsString());
        }
    }

    @Test
    void getPortfolioValue_delegatesToUser() throws Exception {
        try (MockedStatic<User> user = Mockito.mockStatic(User.class)) {
            user.when(() -> User.getPortfolioValue("uid")).thenReturn(9876.54);
            ResponseEntity<JsonObject> resp = service.getPortfolioValue("uid");
            assertEquals(200, resp.getStatusCodeValue());
            assertEquals(9876.54, resp.getBody().get("balance").getAsDouble());
        }
    }
}
