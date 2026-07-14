package com.stockapp.stock_backend.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionTest {
    private static Transaction buyTransaction;
    private static Transaction sellTransaction;
    private static Date now;

    @BeforeAll
    static void setup() throws Exception {
        now = new Date();
        buyTransaction = new Transaction("AAPL", 12, 213, now, true);
        sellTransaction = new Transaction("AAPL", 6, 211, now, false);
    }

    @Test
    void testGetStockSymbol() {
        assertEquals(buyTransaction.getStockSymbol(), "AAPL");
        assertEquals(sellTransaction.getStockSymbol(), "AAPL");
    }

    @Test
    void testGetShares() {
        assertEquals(buyTransaction.getShares(), 12);
        assertEquals(sellTransaction.getShares(), 6);
    }

    @Test
    void testGetAvgPrice() {
        assertEquals(buyTransaction.getPrice(), 213);
        assertEquals(sellTransaction.getPrice(), 211);
    }

    @Test
    void testGetDate() {
        assertEquals(buyTransaction.getDate(), now);
        assertEquals(sellTransaction.getDate(), now);
    }

    @Test
    void getIsBuy() {
        assertEquals(buyTransaction.isBuy(), true);
        assertEquals(sellTransaction.isBuy(), false);
    }
}
