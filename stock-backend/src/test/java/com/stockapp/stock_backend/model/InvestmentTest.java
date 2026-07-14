package com.stockapp.stock_backend.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InvestmentTest {

    private static Investment investment;

    @BeforeEach
    void setup() throws Exception {
        investment = new Investment("AAPL", 12, 213);
    }

    @Test
    void testGetStockSymbol() {
        assertEquals(investment.getStockSymbol(), "AAPL");
    }

    @Test
    void testGetShares() {
        assertEquals(investment.getShares(), 12);
    }

    @Test
    void testGetAvgPrice() {
        assertEquals(investment.getAvgPrice(), 213);
    }

    @Test
    void testIncrementShares() {
        investment.incrementShares(12, 250);
        assertEquals(investment.getAvgPrice(), (double) (213 + 250) / 2);
    }

    @Test
    void testDecrementShares() {
        investment.decrementShares(investment.getShares());
        assertEquals(investment.getShares(), 0);
    }

    @Test
    void testDecrementSharesFail() {
        assertThrows(IndexOutOfBoundsException.class, () ->
            { investment.decrementShares(investment.getShares() + 1);

            });
    }

}
