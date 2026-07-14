package com.stockapp.stock_backend.controller;

import com.stockapp.stock_backend.model.Stock;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockSearchController.class)
@AutoConfigureMockMvc(addFilters = false)
public class StockSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testSearchStocks() throws Exception {
        try (MockedStatic<Stock> mockedStock = Mockito.mockStatic(Stock.class)) {
            String[][] nameMatches = {
                    {"Apple Inc.", "AAPL"},
                    {"Amazon.com", "AMZN"}
            };
            String[][] symbolMatches = {
                    {"Microsoft Corporation", "MSFT"}
            };

            mockedStock.when(() -> Stock.getTopPrefixNames("A", 10)).thenReturn(nameMatches);
            mockedStock.when(() -> Stock.getTopPrefixSymbols("A", 10)).thenReturn(symbolMatches);
            mockedStock.when(() -> Stock.getCurrentPrice(Mockito.anyString())).thenReturn(100.0f);
            mockedStock.when(() -> Stock.getChangePercent(Mockito.anyString())).thenReturn(1.5f);

            mockMvc.perform(get("/api/stock/search")
                            .param("prefix", "A")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].symbol").value("MSFT"));
        }
    }

    @Test
    void testGetMostActiveStocks() throws Exception {
        try (MockedStatic<Stock> mockedStock = Mockito.mockStatic(Stock.class)) {
            String[][] activeStocks = {
                    {"NVIDIA Corporation", "NVDA", "50000000"},
                    {"Tesla, Inc.", "TSLA", "48000000"}
            };

            mockedStock.when(() -> Stock.getMostActiveStocks(10)).thenReturn(activeStocks);
            mockedStock.when(() -> Stock.getCurrentPrice(Mockito.anyString())).thenReturn(250.0f);
            mockedStock.when(() -> Stock.getChangePercent(Mockito.anyString())).thenReturn(-0.75f);

            mockMvc.perform(get("/api/stock/mostactive")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].symbol").value("NVDA"));
        }
    }
}
