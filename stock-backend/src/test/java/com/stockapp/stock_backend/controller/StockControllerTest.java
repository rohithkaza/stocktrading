package com.stockapp.stock_backend.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.stockapp.stock_backend.model.Stock;
import com.stockapp.stock_backend.server.ServerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockController.class)
@AutoConfigureMockMvc(addFilters = false)  // disables security filters (o.w. get 403 Forbidden Error)
public class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServerService serverService;

    @BeforeEach
    void setUp() throws ExecutionException, InterruptedException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("key", "value");

        JsonArray jsonArray = new JsonArray();
        jsonArray.add(jsonObject);

        Mockito.when(serverService.getHistoricalData(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(ResponseEntity.ok(jsonObject));
        Mockito.when(serverService.getCurrentPrice(anyString()))
                .thenReturn(ResponseEntity.ok(jsonObject));
        Mockito.when(serverService.getChange(anyString()))
                .thenReturn(ResponseEntity.ok(jsonObject));
        Mockito.when(serverService.getChangePercent(anyString()))
                .thenReturn(ResponseEntity.ok(jsonObject));
        Mockito.when(serverService.getCompanyDescription(anyString()))
                .thenReturn(ResponseEntity.ok(jsonObject));
        Mockito.when(serverService.getNews(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(ResponseEntity.ok(jsonArray));
    }

    @Test
    void testGetHistoricalData() throws Exception {
        mockMvc.perform(post("/stock/histdata")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockSymbol\":\"AAPL\",\"timeFrame\":\"1d\",\"startDate\":\"2023-01-01\",\"endDate\":\"2023-01-31\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"key\":\"value\"}"));
    }

    @Test
    void testGetPrice() throws Exception {
        mockMvc.perform(post("/stock/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockSymbol\":\"AAPL\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"key\":\"value\"}"));
    }

    @Test
    void testGetChange() throws Exception {
        mockMvc.perform(post("/stock/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockSymbol\":\"AAPL\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"key\":\"value\"}"));
    }

    @Test
    void testGetChangePercent() throws Exception {
        mockMvc.perform(post("/stock/changepercent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockSymbol\":\"AAPL\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"key\":\"value\"}"));
    }

    @Test
    void testGetCompanyDescription() throws Exception {
        mockMvc.perform(post("/stock/description")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockSymbol\":\"AAPL\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"key\":\"value\"}"));
    }

    @Test
    void testGetNews() throws Exception {
        mockMvc.perform(post("/stock/news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockSymbol\":\"AAPL\",\"startDate\":\"2023-01-01\",\"endDate\":\"2023-01-31\",\"limit\":\"1\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"key\":\"value\"}]"));
    }

    @Test
    void testGetStockName() throws Exception {
        try (MockedStatic<Stock> mockedStock = Mockito.mockStatic(Stock.class)) {
            mockedStock.when(() -> Stock.getCompanyName("AAPL"))
                    .thenReturn("Apple Inc.");

            mockMvc.perform(post("/stock/stockname")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"stockSymbol\":\"AAPL\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"name\":\"Apple Inc.\"}"));
        }
    }
}
