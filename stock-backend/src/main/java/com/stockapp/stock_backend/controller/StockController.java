package com.stockapp.stock_backend.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.stockapp.stock_backend.server.ServerService;
import com.stockapp.stock_backend.model.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.io.IOException;
/**
 * Handles API endpoints related to stock data and company information.
 * Includes historical prices, current prices, price changes, news, and company descriptions.
 */
@RestController
@RequestMapping("/stock")
public class StockController {

    private final ServerService serverService;

    @Autowired
    public StockController(ServerService serverService) {
        this.serverService = serverService;
    }

    /**
     * Returns historical stock price data for a given symbol and date range.
     * @param body contains stockSymbol, timeFrame, startDate, endDate
     * @return JSON string of historical data
     */
    @PostMapping("/histdata")
    public ResponseEntity<String> getHistoricalData(@RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {
//        System.out.println(body.toString());
        String stockSymbol = body.get("stockSymbol");
        String timeFrame = body.get("timeFrame");
        String startDate = body.get("startDate");
        String endDate = body.get("endDate");
        ResponseEntity<JsonObject> response = serverService.getHistoricalData(stockSymbol, timeFrame, startDate, endDate);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody().toString());
    }
    /**
     * Returns the current stock price for a given symbol.
     * @param body contains stockSymbol
     * @return JSON string of current price
     */

    @PostMapping("/price")
    public ResponseEntity<String> getPrice(@RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {
        String stockSymbol = body.get("stockSymbol");
        ResponseEntity<JsonObject> response = serverService.getCurrentPrice(stockSymbol);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody().toString());
    }
    /**
     * Returns the absolute price change for a given stock symbol.
     * @param body contains stockSymbol
     * @return JSON string of price change
     */

    @PostMapping("/change")
    public ResponseEntity<String> getChange(@RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {
        String stockSymbol = body.get("stockSymbol");
        ResponseEntity<JsonObject> response = serverService.getChange(stockSymbol);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody().toString());
    }
    /**
     * Returns the percentage price change for a given stock symbol.
     * @param body contains stockSymbol
     * @return JSON string of percent change
     */

    @PostMapping("/changepercent")
    public ResponseEntity<String> getChangePercent(@RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {
        String stockSymbol = body.get("stockSymbol");
        ResponseEntity<JsonObject> response = serverService.getChangePercent(stockSymbol);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody().toString());
    }
    /**
     * Returns the company description for a given stock symbol.
     * @param body contains stockSymbol
     * @return JSON string of company description
     */
    @PostMapping("/description")
    public ResponseEntity<String> getCompanyDescription(@RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {
        String stockSymbol = body.get("stockSymbol");
        ResponseEntity<JsonObject> response = serverService.getCompanyDescription(stockSymbol);
        //System.out.println("RESPONSE BODY IS "+response.getBody().toString());
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody().toString());
    }
    /**
     * Returns recent news articles related to a stock.
     * @param body contains stockSymbol, startDate, endDate, and limit
     * @return JSON string of news array
     */
    @PostMapping("/news")
    public ResponseEntity<String> getNews(@RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {
        System.out.println("News: " + body);
        String stockSymbol = body.get("stockSymbol");
        String startDate = body.get("startDate");
        String endDate = body.get("endDate");
        int limit = Integer.parseInt(body.get("limit"));
        ResponseEntity<JsonArray> response = serverService.getNews(stockSymbol, startDate, endDate, limit);
        System.out.println(response.getBody());
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody().toString());
    }
    /**
     * Returns the full company name for a given stock symbol.
     * @param body contains stockSymbol
     * @return JSON string with name field
     */
    @PostMapping("/stockname")
    public ResponseEntity<String> getStockName(@RequestBody Map<String, String> body) throws ExecutionException, InterruptedException, IOException {
        String stockSymbol = body.get("stockSymbol");
        String companyName = Stock.getCompanyName(stockSymbol);

        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("name", companyName);

        return ResponseEntity.ok(responseJson.toString());
    }
}