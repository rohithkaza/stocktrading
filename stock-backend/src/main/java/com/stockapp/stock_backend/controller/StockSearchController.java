package com.stockapp.stock_backend.controller;

import com.stockapp.stock_backend.model.Stock;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/stock")
public class StockSearchController {

private static final int MAX_RESULTS = 10;

/**
* Search for stocks by prefix
*
* @param prefix The search prefix
* @return A list of stocks matching the prefix
*/
@GetMapping("/search")
public ResponseEntity<List<Map<String, Object>>> searchStocks(@RequestParam String prefix) {
    List<Map<String, Object>> results = new ArrayList<>();
    try {
        prefix = prefix.toUpperCase(); // Make search case-insensitive


        // If we didn't find enough priority matches, add regular search results
        if (results.size() < MAX_RESULTS) {
            // Search by both name and symbol, combining results
            String[][] nameMatches = Stock.getTopPrefixNames(prefix, MAX_RESULTS);
            String[][] symbolMatches = Stock.getTopPrefixSymbols(prefix, MAX_RESULTS);
            // Process symbol matches first (more relevant for stock tickers)
            addMatchesToResults(symbolMatches, results);

            // If we still need more results, add name matches
            if (results.size() < MAX_RESULTS) {
                addMatchesToResults(nameMatches, results);
                }
        }

        // Trim results to max size
        if (results.size() > MAX_RESULTS) {
            results = results.subList(0, MAX_RESULTS);
        }

        return ResponseEntity.ok(results);
    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(new ArrayList<>());
    }
}

@GetMapping("/mostactive")
public ResponseEntity<List<Map<String, Object>>> getMostActiveStocks() {
    List<Map<String, Object>> results = new ArrayList<>();
    try {
        // Search by both name and symbol, combining results
        String[][] symbolMatches = Stock.getMostActiveStocks(MAX_RESULTS);
        // Process symbol matches first (more relevant for stock tickers)
        addMostActiveMatchesToResults(symbolMatches, results);

        return ResponseEntity.ok(results);
    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(new ArrayList<>());
    }
}

/**
* Find popular stocks that match the prefix
*/



private void addMostActiveMatchesToResults(String[][] matches, List<Map<String, Object>> results) throws IOException {
    for (String[] match : matches) {
        if (match[0] != null && match[1] != null && match[2] != null) {
            // Check if this symbol is already in the results
            boolean isDuplicate = false;
            for (Map<String, Object> existing : results) {
                if (match[1].equals(existing.get("symbol"))) {
                    isDuplicate = true;
                    break;
                }
            }

            if (!isDuplicate && results.size() < MAX_RESULTS) {
                Map<String, Object> stockInfo = new HashMap<>();
                stockInfo.put("name", match[0]);
                stockInfo.put("symbol", match[1]);
                stockInfo.put("volume", match[2]);
                // Try to get current price, if available
                try {
                    float price = Stock.getCurrentPrice(match[1]);
                    float change = Stock.getChangePercent(match[1]);
                    stockInfo.put("price", price);
                    stockInfo.put("change", change);
                    stockInfo.put("isPositive", change >= 0);
                } catch (Exception e) {
                    stockInfo.put("price", 0.0);
                    stockInfo.put("change", 0.0);
                    stockInfo.put("isPositive", true);
                }

                results.add(stockInfo);
            }
        }
    }
}

/**
* Add matches to the results list, avoiding duplicates
*/
private void addMatchesToResults(String[][] matches, List<Map<String, Object>> results) throws IOException {
    for (String[] match : matches) {
        if (match[0] != null && match[1] != null) {
            // Check if this symbol is already in the results
            boolean isDuplicate = false;
            for (Map<String, Object> existing : results) {
                if (match[1].equals(existing.get("symbol"))) {
                    isDuplicate = true;
                    break;
                }
            }

            if (!isDuplicate && results.size() < MAX_RESULTS) {
                Map<String, Object> stockInfo = new HashMap<>();
                stockInfo.put("name", match[0]);
                stockInfo.put("symbol", match[1]);
                // Try to get current price, if available
                try {
                    float price = Stock.getCurrentPrice(match[1]);
                    float change = Stock.getChangePercent(match[1]);
                    stockInfo.put("price", price);
                    stockInfo.put("change", change);
                    stockInfo.put("isPositive", change >= 0);
                } catch (Exception e) {
                    stockInfo.put("price", 0.0);
                    stockInfo.put("change", 0.0);
                    stockInfo.put("isPositive", true);
                }

                results.add(stockInfo);
            }
        }
    }
}


/**
* Get company name for a stock symbol (simplified implementation)
*/
private String getCompanyName(String symbol) {
    Map<String, String> companyNames = new HashMap<>();
    companyNames.put("AAPL", "Apple Inc.");
    companyNames.put("MSFT", "Microsoft Corporation");
    companyNames.put("GOOGL", "Alphabet Inc.");
    companyNames.put("GOOG", "Alphabet Inc.");
    companyNames.put("AMZN", "Amazon.com, Inc.");
    companyNames.put("META", "Meta Platforms, Inc.");
    companyNames.put("TSLA", "Tesla, Inc.");
    companyNames.put("NVDA", "NVIDIA Corporation");
    companyNames.put("JPM", "JPMorgan Chase & Co.");
    companyNames.put("BAC", "Bank of America Corporation");
    companyNames.put("WMT", "Walmart Inc.");
    companyNames.put("DIS", "The Walt Disney Company");
    companyNames.put("NFLX", "Netflix, Inc.");
    companyNames.put("INTC", "Intel Corporation");
    companyNames.put("AMD", "Advanced Micro Devices, Inc.");
    companyNames.put("IBM", "International Business Machines Corporation");
    companyNames.put("CSCO", "Cisco Systems, Inc.");
    companyNames.put("ORCL", "Oracle Corporation");
    companyNames.put("CRM", "Salesforce, Inc.");
    companyNames.put("ADBE", "Adobe Inc.");
    companyNames.put("V", "Visa Inc.");
    companyNames.put("MA", "Mastercard Incorporated");

    return companyNames.getOrDefault(symbol, symbol + " Corp");
    }
}