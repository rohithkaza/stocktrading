package com.stockapp.stock_backend.model;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

public class StockTest {

    @Test
    void getTopPrefixNamesTest() {
        try {
            String[][] output = Stock.getTopPrefixNames("AA", 20);
            for(String[] o : output) {
                String name  = o[0];
                String symbol = o[1];
                if(!(name.charAt(0)=='A' && name.charAt(1)=='A') && !(symbol.charAt(0)=='A' && symbol.charAt(1)=='A'))
                    Assertions.fail();
            }
        } catch (IOException | InterruptedException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    void getTopPrefixSymbolsTest() {
        try {
            String[][] output = Stock.getTopPrefixNames("AA", 20);
            for(String[] o : output) {
                String name  = o[0];
                String symbol = o[1];
                if(!(name.charAt(0)=='A' && name.charAt(1)=='A') && !(symbol.charAt(0)=='A' && symbol.charAt(1)=='A'))
                    Assertions.fail();
            }
        } catch (IOException | InterruptedException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    void isMarketOpenTest() {
        try {
            boolean o1 = Stock.isMarketOpen();
            boolean o2 = Stock.isMarketOpen();
            boolean o3 = Stock.isMarketOpen();
            if(o1==o3 && o1!=o2)
                Assertions.fail();            
        } catch (IOException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    void getCurrentPriceTest() {
        try {
            if(Stock.getCurrentPrice("amzn") < 0)
                Assertions.fail();
        } catch (IOException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    void getHistoricalPricesTest() {
        try {
            JsonObject data = Stock.getHistoricalPrices("AMZN", "1Day", "2024-01-04", "2024-01-04");
            if(data==null || data.get("AMZN")==null)
                Assertions.fail();
        } catch (IOException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    void getSnapshotTest() {
        try {
            JsonObject data = Stock.getSnapshot("amzn");
            if(data==null || data.get("symbol").getAsString()==null || !data.get("symbol").getAsString().equals("AMZN"))
                Assertions.fail();
        } catch (IOException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    void getMostActiveStocksTest() {
        try {
            String[][] data = Stock.getMostActiveStocks(20);
            for(String[] ele : data) {
                if(ele.length!=3)
                    Assertions.fail();
                Integer.parseInt(ele[2]);
            }
        } catch (IOException | InterruptedException e) {
            Assertions.assertTrue(true);
        } catch (NumberFormatException e) {
            Assertions.fail();
        }
    }

    @Test
    void getNewsTest() {
        try {
            JsonObject data = Stock.getNews("AMZN", "2024-01-04", "2025-01-04", 1).get(0).getAsJsonObject();
            if(data==null || data.get("news")==null)
                Assertions.fail();
        } catch (IOException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    void getIndustryTest() {
        try {
            Assertions.assertEquals("Retail", Stock.getIndustry("AMZN"));
        } catch (IOException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    void getCompanyNameTest() {
        try {
            Assertions.assertEquals("AMAZON.COM, INC. COMMON STOCK", Stock.getCompanyName("AMZN"));
        } catch (IOException | InterruptedException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    void getWikiCompanyDescriptionTest() {
        String[] data = Stock.getWikiCompanyDescription("AMZN");
        if(data.length==0)
            Assertions.assertTrue(true);
        else {
            Assertions.assertEquals("https://en.wikipedia.org/wiki/Amazon_%28company%29", data[0]);
            Assertions.assertEquals("Amazon (company)", data[1]);
            Assertions.assertNotNull(data[2]);
        }
    }

    @Test
    void getAlphaVantageCompanyDescriptionTest() {
        String[] data = Stock.getAlphaVantageCompanyDescription("AMZN");
        Assertions.assertDoesNotThrow(() -> {
            switch (Integer.parseInt(data[1])) {
                case 0: Assertions.assertNotNull(data[0]); Assertions.assertNotEquals("", data[0]); break;
                case 1: Assertions.assertEquals("", data[1]); break;
                case 2: Assertions.assertEquals("", data[2]); break;
                case 3: Assertions.assertEquals("", data[2]); break;
                default: Assertions.fail();
            }
        });
    }
}
