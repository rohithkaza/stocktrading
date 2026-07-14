package com.stockapp.stock_backend.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.google.api.client.json.Json;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Stock {
    private static final String apiKey = "PKV7IT6T9DS9PIGGN2CB"; // "PK5IU1TGMA4RLAJC34J5";
    private static final String apiSecretKey = "pV4tjwaoP7CY0heazFhVhVztvnbU9Cg3FHoiUep0"; //"APQWzxaRmxxG5PtuMUSkVqE1dqWxCeHvX6AKlTxH";
    private static String[] allNames = null;
    private static String[] allSymbols = null;
    private static String[] sortedNames = null;
    private static String[] sortedSymbols = null;
    private static int[] sortedNameInds = null;
    private static int[] sortedSymbolInds = null;


    /**
     * Function to find stocks by company name
     * @param prefix start of a company name
     * @param count number of stocks to query for
     * @return An array of arrays with count elements where each element contains the [company name, stock ticker]
     * @throws IOException
     * @throws InterruptedException
     */
    public static String[][] getTopPrefixNames(String prefix, int count) throws IOException, InterruptedException {
        if(allNames == null)
            populateNamesSymbolsFields();

        prefix = prefix.toUpperCase();
        String nextPrefix = prefix;
        boolean allZ = false;
        for(int i = nextPrefix.length()-1; i >= 0; i--) {
            if(nextPrefix.charAt(i)!='Z') {
                nextPrefix = nextPrefix.substring(0, i) + (char)(nextPrefix.charAt(i)+1);
                break;
            } else if(i==0) {
                allZ = true;
            }
        }

        int prefixInd = binarySearch(sortedNames, prefix);
        int excEndInd = allZ ? sortedNames.length : binarySearch(sortedNames, nextPrefix);

        int trueLength = Math.min(Math.min(count, sortedNames.length-prefixInd), excEndInd-prefixInd);
        String[][] outputNamesSymbols = new String[trueLength][2];
        for(int i = prefixInd; i < prefixInd+trueLength; i++) {
            outputNamesSymbols[i-prefixInd][0] = sortedNames[i];
            outputNamesSymbols[i-prefixInd][1] = allSymbols[sortedNameInds[i]];
        }

        return outputNamesSymbols;
    }

    /**
     * Function to find stocks by stock ticker
     * @param prefix prefix of stock ticker
     * @param count number of stocks to query for
     * @return An array of arrays with count elements where each element contains the [company name, stock ticker]
     * @throws IOException
     * @throws InterruptedException
     */
    public static String[][] getTopPrefixSymbols(String prefix, int count) throws IOException, InterruptedException {
        if(allSymbols == null)
            populateNamesSymbolsFields();

        prefix = prefix.toUpperCase();
        String nextPrefix = prefix;
        boolean allZ = false;
        for(int i = nextPrefix.length()-1; i >= 0; i--) {
            if(nextPrefix.charAt(i)!='Z') {
                nextPrefix = nextPrefix.substring(0, i) + (char)(nextPrefix.charAt(i)+1);
                break;
            } else if(i==0) {
                allZ = true;
            }
        }

        int prefixInd = binarySearch(sortedSymbols, prefix);
        int excEndInd = allZ ? sortedSymbols.length : binarySearch(sortedSymbols, nextPrefix);

        int trueLength = Math.min(Math.min(count, sortedSymbols.length-prefixInd), excEndInd-prefixInd);
        String[][] outputNamesSymbols = new String[trueLength][2];
        for(int i = prefixInd; i < prefixInd+trueLength; i++) {
            outputNamesSymbols[i-prefixInd][0] = allNames[sortedSymbolInds[i]];
            outputNamesSymbols[i-prefixInd][1] = sortedSymbols[i];
        }

        return outputNamesSymbols;
    }

    /**
     * Function to return whether the stock market is open or not
     * @return true if the market is open and false otherwise
     * @throws IOException
     */
    public static boolean isMarketOpen() throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://paper-api.alpaca.markets/v2/clock")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("APCA-API-KEY-ID", Stock.apiKey)
                .addHeader("APCA-API-SECRET-KEY", Stock.apiSecretKey)
                .build();

        Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                String errorInfo = "Unknown error";
                throw new IOException(errorInfo);
            }

            if (response.body() == null) {
                throw new IOException("Empty response body");
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

            System.out.println(jsonResponse.toString());

            return jsonResponse.getAsJsonPrimitive("is_open").getAsBoolean();
    }

    /**
     * Function to return the current price of the stock (with a 15 minute delay)
     * @param stockSymbol stock symbol for the stock to search for
     * @return stock price for the given symbol, giving closing price if market is closed
     * @throws IOException
     */
    public static float getCurrentPrice(String stockSymbol) throws IOException {

        OkHttpClient client = new OkHttpClient();

        // Documentation for this: https://docs.alpaca.markets/reference/stocklatestquotes-1
        Request request = new Request.Builder()
                .url("https://data.alpaca.markets/v2/stocks/trades/latest?symbols="+stockSymbol)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("APCA-API-KEY-ID", Stock.apiKey)
                .addHeader("APCA-API-SECRET-KEY", Stock.apiSecretKey)
                .build();

        // Probably should check the response to see if all good
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorInfo = "Unknown error";
                switch (response.code()) {
                    case 400:
                        errorInfo = "Invalid query parameter";
                    case 403:
                        errorInfo = "Invalid API keys";
                    case 429:
                        errorInfo = "Too many requests";
                    case 500:
                        errorInfo = "API internal server error";
                }
                throw new IOException(errorInfo);
            }

            // Example respBody: {"quotes":{"AAPL":{"ap":245.01,"as":1,"ax":"V","bp":240,"bs":1,"bx":"V","c":["R"],"t":"2025-02-26T20:59:57.550854527Z","z":"C"}}}
            // Parse response JSON
            if (response.body() == null) {
                throw new IOException("Empty response body");
            }
            String respBody = response.body().string();

            JsonObject data = JsonParser.parseString(respBody).getAsJsonObject();
            JsonObject trades = data.get("trades").getAsJsonObject();
            JsonObject stockinfo;
            float price;
            if (trades != null && trades.get(stockSymbol) != null) {
                stockinfo = trades.get(stockSymbol).getAsJsonObject();
            } else {
                throw new IOException(stockSymbol + " has no trades");
            }
            if (stockinfo != null && stockinfo.get("p") != null) {
                price = stockinfo.get("p").getAsFloat();
            } else {
                throw new IOException(stockSymbol + " has no price");
            }

            return price;
        } catch (IOException e) {
            throw new IOException("No response");
        }
    }

    /**
     * Function to get the historical prices for a given stock
     * @param stockSymbol stock ticker to get historical prices for
     * @param timeframe timeframe of each data point (e.g. 5 minute increments)
     * @param startDate start date of data
     * @param endDate end data of data
     * @return JSON array of historical stock data
     * @throws IOException
     */
    public static JsonObject getHistoricalPrices(String stockSymbol, String timeframe, String startDate, String endDate) throws IOException {

        OkHttpClient client = new OkHttpClient();

        JsonObject allData = new JsonObject();
        JsonArray allStockBars = new JsonArray();
        String nextPageToken = null;
        String url = "";

        do {
            // construct the request URL
            url = "https://data.alpaca.markets/v2/stocks/bars?symbols="+stockSymbol+
                    "&timeframe="+timeframe+
                    "&start="+startDate+
                    "&end=" + endDate;

            // if there's a pagination token, include it
            if (nextPageToken != null) {
                url += "&page_token="+nextPageToken;
            }

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("APCA-API-KEY-ID", Stock.apiKey)
                    .addHeader("APCA-API-SECRET-KEY", Stock.apiSecretKey)
                    .build();

            // Execute the request
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorInfo = "Unknown error";
                    switch (response.code()) {
                        case 400:
                            errorInfo = "Invalid query parameter";
                        case 403:
                            errorInfo = "Invalid API keys";
                        case 429:
                            errorInfo = "Too many requests";
                        case 500:
                            errorInfo = "API internal server error";
                    }
                    throw new IOException(errorInfo);
                }

                // Parse response JSON
                if (response.body() == null) {
                    throw new IOException("Empty response body");
                }
                String responseBody = response.body().string();
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                 System.out.println(jsonResponse.toString());

                JsonObject bars = jsonResponse.getAsJsonObject("bars");

                if (bars != null && bars.has(stockSymbol)) {
                    JsonArray stockBars = bars.getAsJsonArray(stockSymbol);
                    if (stockBars != null) {
                        allStockBars.addAll(stockBars);
                    }
                }

                // Get next page token
                nextPageToken = jsonResponse.has("next_page_token") && !jsonResponse.get("next_page_token").isJsonNull()
                        ? jsonResponse.get("next_page_token").getAsString()
                        : null;

            } catch (IOException e) {
                throw new IOException("No Response");
            }
        } while (nextPageToken != null);  // Continue while there's more data

        allData.add(stockSymbol, allStockBars);
        return allData;
    }

    /**
     * Function to get snapshot of important info for a stock
     * @param stockSymbol stock ticker for wanted stock
     * @return JSON Object with latest trade, latest quote, minute bar, daily bar, and previous daily bar data for given stock
     * @throws IOException
     */
    public static JsonObject getSnapshot(String stockSymbol) throws IOException {

        OkHttpClient client = new OkHttpClient();

        JsonObject allData;

        String url = "https://data.alpaca.markets/v2/stocks/"+stockSymbol+"/snapshot";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("APCA-API-KEY-ID", Stock.apiKey)
                .addHeader("APCA-API-SECRET-KEY", Stock.apiSecretKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorInfo = "Unknown error";
                switch (response.code()) {
                    case 400:
                        errorInfo = "Invalid query parameter";
                    case 403:
                        errorInfo = "Invalid API keys";
                    case 429:
                        errorInfo = "Too many requests";
                    case 500:
                        errorInfo = "API internal server error";
                }
                throw new IOException(errorInfo);
            }

            if (response.body() == null) {
                throw new IOException("Empty response body");
            }

            String responseBody = response.body().string();
            allData = JsonParser.parseString(responseBody).getAsJsonObject();

        } catch (IOException e) {
            throw new IOException("No Response");
        }

        return allData;
    }

    /**
     * Function to get percent change since last day for a stock
     * @param stockSymbol stock ticker for wanted stock
     * @return percent change for wanted stock
     * @throws IOException
     */
    public static float getChangePercent(String stockSymbol) throws IOException {
        JsonObject snapshotData = getSnapshot(stockSymbol);
        float currClose = snapshotData.get("dailyBar").getAsJsonObject().get("c").getAsFloat();
        float prevClose = snapshotData.get("prevDailyBar").getAsJsonObject().get("c").getAsFloat();
        float change = currClose - prevClose;
        float changePercent = (change/prevClose) * 100;

        return changePercent;
    }

    /**
     * Function to get the most active stocks
     * @param count number of stocks to receive (min 1, max 100)
     * @return JSON Object with count number of most active stocks
     * @throws IOException
     * @throws InterruptedException
     */
    public static String[][] getMostActiveStocks(int count) throws IOException, InterruptedException {

        OkHttpClient client = new OkHttpClient();

        JsonObject allData;

        String url = "https://data.alpaca.markets/v1beta1/screener/stocks/most-actives?by=trades&top="+ Math.max(Math.min(count, 100), 1);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("APCA-API-KEY-ID", Stock.apiKey)
                .addHeader("APCA-API-SECRET-KEY", Stock.apiSecretKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorInfo = "Unknown error";
                switch (response.code()) {
                    case 400:
                        errorInfo = "Invalid query parameter";
                    case 403:
                        errorInfo = "Invalid API keys";
                    case 429:
                        errorInfo = "Too many requests";
                    case 500:
                        errorInfo = "API internal server error";
                }
                throw new IOException(errorInfo);
            }

            if (response.body() == null) {
                throw new IOException("Empty response body");
            }

            String responseBody = response.body().string();
            System.out.println("Response: " + responseBody);
            allData = JsonParser.parseString(responseBody).getAsJsonObject();

            String[][] output = new String[count][3];

            int activeStockInd = 0;
            for(JsonElement ele : allData.get("most_actives").getAsJsonArray()) {
                output[activeStockInd][1] = ele.getAsJsonObject().getAsJsonPrimitive("symbol").getAsString();
                output[activeStockInd][0] = getCompanyName(output[activeStockInd][1]);
                output[activeStockInd][2] = "" + ele.getAsJsonObject().getAsJsonPrimitive("trade_count").getAsInt();

                activeStockInd += 1;
            }
            return output;

        } catch (IOException e) {
            throw new IOException("No Response");
        }
    }

    /**
     * Function to get news for a given stock within a given time frame
     * @param stockSymbol stock ticker for wanted stock
     * @param startDate start date for news
     * @param endDate end date for news
     * @param limit max number of news to receive
     * @return JSON array with news articles for wanted stock
     * @throws IOException
     */
    public static JsonArray getNews(String stockSymbol, String startDate, String endDate, int limit) throws IOException {

        OkHttpClient client = new OkHttpClient();

        JsonArray allArticles = new JsonArray();
        String nextPageToken = null;

        String url = "";

        do {
            // construct the request URL
            url = "https://data.alpaca.markets/v1beta1/news?start="+startDate+
                    "&end=" + endDate +
                    "&sort=desc&symbols="+stockSymbol+
                    "&limit="+limit;

            // if there's a pagination token, include it
            if (nextPageToken != null) {
                url += "&page_token="+nextPageToken;
            }

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("APCA-API-KEY-ID", Stock.apiKey)
                    .addHeader("APCA-API-SECRET-KEY", Stock.apiSecretKey)
                    .build();

            // Execute the request
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorInfo = "Unknown error";
                    switch (response.code()) {
                        case 400:
                            errorInfo = "Invalid query parameter";
                        case 403:
                            errorInfo = "Invalid API keys";
                        case 429:
                            errorInfo = "Too many requests";
                        case 500:
                            errorInfo = "API internal server error";
                    }
                    throw new IOException(errorInfo);
                }

                // Parse response JSON
                if (response.body() == null) {
                    throw new IOException("Empty response body");
                }
                String responseBody = response.body().string();
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                JsonArray news = jsonResponse.getAsJsonArray("news");

                if (news != null) {
                    for (JsonElement article : news) {
                        allArticles.add(article.getAsJsonObject());
                    }
                }

                // Get next page token
                nextPageToken = jsonResponse.has("next_page_token") && !jsonResponse.get("next_page_token").isJsonNull()
                        ? jsonResponse.get("next_page_token").getAsString()
                        : null;

            } catch(IOException e) {
                throw new IOException("No response");
            }
        } while (nextPageToken != null);  // Continue while there's more data

        return allArticles;
    }

    /**
     * Function to get the type of industry for a stock
     * @param stockSymbol stock ticker for wanted stock
     * @return type of industry for the wanted stock
     * @throws IOException
     */
    public static String getIndustry(String stockSymbol) throws IOException {
        final String api_key_finnhub = "cukj6npr01qo08i8el00cukj6npr01qo08i8el0g";
        final String api_secret_key_finnhub = "cukj6npr01qo08i8el1g";

        OkHttpClient client = new OkHttpClient();
        String url = "https://finnhub.io/api/v1/stock/profile2?symbol=" + stockSymbol + "&token=" + api_key_finnhub;

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
//                System.out.println(response.body().string());
                String jsonString = response.body().string();
                JsonObject body = JsonParser.parseString(jsonString).getAsJsonObject();
                System.out.println(body.toString());
                if (!body.has("finnhubIndustry")) {
                    throw new IOException("No finnhub industry");
                }
                return body.get("finnhubIndustry").getAsString();
            }
            else {
                String errorInfo = "Unknown error";
                switch (response.code()) {
                    case 400:
                        errorInfo = "Invalid query parameter";
                    case 403:
                        errorInfo = "Invalid API keys";
                    case 429:
                        errorInfo = "Too many requests";
                    case 500:
                        errorInfo = "API internal server error";
                }
                throw new IOException(errorInfo);
            }
        }
    }

    /**
     * Function to get company name for the stock symbol on alpaca
     * @param stockSymbol stock ticker for wanted stock
     * @return company name, or empty string if not available
     * @throws IOException
     * @throws InterruptedException
     */
    // If stock is on alpaca, get corresponding company name for stockSymbol, else empty string.
    public static String getCompanyName(String stockSymbol) throws IOException, InterruptedException {
        if(allNames == null)
            populateNamesSymbolsFields();

        stockSymbol = stockSymbol.toUpperCase();
        int indSortedSymbol = binarySearch(sortedSymbols, stockSymbol);
        if((indSortedSymbol >= sortedSymbols.length) || !sortedSymbols[indSortedSymbol].equals(stockSymbol))
            return "";
        
        int unsortedNameInd = sortedSymbolInds[indSortedSymbol];
        return allNames[unsortedNameInd];
    }

    /**
     * Function to get company description from wikipedia article
     * @param stockSymbol stock ticker for wanted stock
     * @return a description of the company taken from their wikipedia page if it exists, and nothing otherwise
     */
    public static String[] getWikiCompanyDescription(String stockSymbol) {
        try {
            String appCompanyName = getCompanyName(stockSymbol);
            String[] a = getClosestMatchingWikiCompany(appCompanyName, "src/main/java/com/nasdaqCompanyNames.txt", "src/main/java/com/fmtedNasdaqCompanyNames.txt");
            String[] b = getClosestMatchingWikiCompany(appCompanyName, "src/main/java/com/nyseCompanyNames.txt", "src/main/java/com/fmtedNyseCompanyNames.txt");
            String closestWikiTitle = (a[1].length() > b[1].length()) ? a[0] : b[0];
            // Note this might be encoded incorrectly.
            String wikiTitlePartOfUrl = wikiURLEncodeTitle(closestWikiTitle);
            //System.out.println(appCompanyName);
            //System.out.println(a[0]);
            //System.out.println(a[1]);
            //System.out.println(b[0]);
            //System.out.println(b[1]);
            //System.out.println(!companyWikiPageHasExternalContentBanners(wikiTitlePartOfUrl));
            if(!closestWikiTitle.equals("") && !companyWikiPageHasExternalContentBanners(wikiTitlePartOfUrl)) {
                //System.out.println("fvjkdfnvkdjfdkvjdfv");
                ArrayList<String> output = new ArrayList<>();

                String urlStr = "https://en.wikipedia.org/wiki/"+wikiTitlePartOfUrl;
                output.add(urlStr);
                output.add(closestWikiTitle);
                Document doc = Jsoup.connect(urlStr)
                .userAgent("Mozilla/5.0")
                .timeout(2000)
                .get();

                Element firstParagraph = null;
                for(Element e : doc.select("p")) {
                    if(e.text().equals(""))
                        continue;
                    firstParagraph = e;
                    output.add(e.text());
                    break;
                }
                for(Element e : firstParagraph.select("sup.reference")) {
                    Element anchor = e.selectFirst("a[href]");
                    if(anchor==null)
                        continue;
                    String citationNumber = anchor.text();
                    output.add(citationNumber);
                    String href = anchor.attr("href");
                    //System.out.println(href);
                    Element footnote = doc.getElementById(href.charAt(0)=='#' ? href.substring(1) : href);
                    output.add(footnote==null ? "" : citationNumber.substring(1, citationNumber.length()-1)+". "+footnote.text());
                }
                return output.toArray(new String[output.size()]);
            }
            return new String[0];
        } catch(Exception e) {
            System.out.println("wikicompdescp ERROR "+e.getMessage());
            return new String[0];
        }

    }

    /**
     * Function to get the company description from AlphaVantage
     * @param stockSymbol stock ticker for wanted stock
     * @return If api active, stock is in api, and rate limit not hit, return [description string, "0"].
     *         Otherwise, if api inactive, return ["", "3"]; if rate limit, return ["", "2"]; if stock not in api,
     *         return ["", "1"] (with priority in that order).
     */
    public static String[] getAlphaVantageCompanyDescription(String stockSymbol) {
        String alpha_key = "8RKAPYO1P6G61DJ3";
        String urlStr = "https://www.alphavantage.co/query?function=OVERVIEW&symbol="+stockSymbol.toUpperCase()+"&apikey="+alpha_key;

        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
    
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    
            String inputLine;
            StringBuilder content = new StringBuilder();
            while(((inputLine = in.readLine()) != null)) {
                content.append(inputLine);
            }
            in.close();
    
            JSONObject json = new JSONObject(content.toString());
    
            if(json.has("Information")) { // rate limit reached
                return new String[]{"", "2"};
            }
            if(json.has("Description")) {
                String description = json.getString("Description");
                return new String[]{description, "0"};
            } else { // json will be empty, i.e., stock not in api
                return new String[]{"", "1"};
            }
        } catch(IOException e) {
            return new String[]{"", "3"};
        }

    }

    /**
     * Function to get the closest match to the given company.
     * @param appCompanyName Wanted company name
     * @param wikiCompanyDocPath path to wikipedia page
     * @param formattedWikiCompanyDocPath formatted path to wikipedia page
     * @return formatted title which is the longest prefix of appCompanyName, and the corr. actual title
     *          in format [actual title, formatted title].
     *          Outputs ["", ""] if no formatted title is a prefix.
     */
    private static String[] getClosestMatchingWikiCompany(String appCompanyName, String wikiCompanyDocPath, String formattedWikiCompanyDocPath) {
        try {
            appCompanyName = appCompanyName.toUpperCase();
            BufferedReader br = new BufferedReader(new FileReader(formattedWikiCompanyDocPath));
            int lineNum = 1;
            String line;
            int closestLength = 0;
            int closestLine = 0;
            String closestText = "";
            
            while((line = br.readLine()) != null) {
                if(line.equals("AMAZON"))
                        System.out.println("AMAZON FOUND");
                if(line.equals(appCompanyName) || (appCompanyName.startsWith(line) && !Character.isLetter(appCompanyName.charAt(line.length())) && line.length() > closestLength)) {
                    closestLength = line.length();
                    closestLine = lineNum;
                    closestText = line;
                    if(line.equals("AMAZON"))
                        System.out.println("UPDATED");
                } else {
                    if(line.equals("AMAZON"))
                        System.out.println("NOT UPDATED");
                }
                lineNum++;
            }
    
            br.close();
            line = "";
            br = new BufferedReader(new FileReader(wikiCompanyDocPath));
            for(lineNum = 0; lineNum < closestLine; lineNum++)
                line = br.readLine();
            br.close();
            return new String[]{line, closestText};
        } catch(IOException e) {
            System.out.println("ERROR FOUND " + e.getMessage());
            return new String[]{"", ""};
        }
    }

    /**
     * Function to check that the page does not contain external content banner
     * @param wikiTitlePartOfUrl wikipedia url
     * @return true if banner exists, false otherwise
     * @throws IOException
     */
    private static boolean companyWikiPageHasExternalContentBanners(String wikiTitlePartOfUrl) throws IOException {
        String urlStr = "https://en.wikipedia.org/w/api.php?action=query&prop=revisions&titles="+wikiTitlePartOfUrl+"&rvslots=main&rvprop=content&formatversion=2&format=json";
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString().indexOf("{{Copied")!=-1 || response.toString().indexOf("{{Content from")!=-1 || response.toString().indexOf("{{Text from")!=-1;
    }

    /**
     * Function to encode the wikipedia article title as appropriate for the webpage's URL
     * @param title title of wiki page
     * @return encoded title
     */
    private static String wikiURLEncodeTitle(String title) {
        try {
            title = URLEncoder.encode(title, "UTF-8");
        }
        catch(UnsupportedEncodingException e) { // This should never trigger since "UTF-8" is valid.
            return "";
        }
        String newTitle = "";
        for(int i = 0; i < title.length(); i++) {
            switch (title.charAt(i)) {
                case '+': newTitle += "_"; break;
                case '(': newTitle += "%28"; break;
                case ')': newTitle += "%29"; break;
                default: newTitle += title.charAt(i);
            }
        }
        return newTitle;
    }

    /**
     * Function to populate all name symbols with their required fields
     * @throws IOException
     * @throws InterruptedException
     */
    private static void populateNamesSymbolsFields() throws IOException, InterruptedException {
        String[][] allNamesSymbols = getAllNamesSymbols();
        allNames = allNamesSymbols[0];
        allSymbols = allNamesSymbols[1];
        sortedNames = Arrays.copyOf(allNames, allNames.length);
        Arrays.sort(sortedNames);
        sortedSymbols = Arrays.copyOf(allSymbols, allSymbols.length);
        Arrays.sort(sortedSymbols);
        sortedNameInds = argSort(allNames);
        sortedSymbolInds = argSort(allSymbols);
    }

    /**
     *
     * @return function to get all name symbols
     * @throws IOException
     * @throws InterruptedException
     */
    private static String[][] getAllNamesSymbols() throws IOException, InterruptedException {
        String[] command = {"curl", "--request", "GET", "--url", "https://paper-api.alpaca.markets/v2/assets?status=active&asset_class=us_equity&attributes=", "--header", "APCA-API-KEY-ID: PKV7IT6T9DS9PIGGN2CB", "--header", "APCA-API-SECRET-KEY: pV4tjwaoP7CY0heazFhVhVztvnbU9Cg3FHoiUep0", "--header", "accept: application/json"};
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();
        
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        ArrayList<String> names = new ArrayList<String>(); // it seems there are sometimes duplicate names.
        ArrayList<String> symbols = new ArrayList<String>(); // it seems there are never any duplicate symbols.
        
        while((line = inputReader.readLine()) != null) { // it seems this always will only be a single "line" with a huge amount of text.
            Matcher nameMatcher = Pattern.compile("\"name\":\"([^\"]*)\"").matcher(line);
            Matcher symbolMatcher = Pattern.compile("\"symbol\":\"([^\"]*)\"").matcher(line);

            if(nameMatcher.find()) {
                do {
                    String name = nameMatcher.group(1);
                    name = name.replaceAll("\\\\u0026", "&");
                    names.add(name.toUpperCase());
                } while(nameMatcher.find());
            }
            if(symbolMatcher.find()) {
                do {
                    String symbol = symbolMatcher.group(1);
                    symbols.add(symbol);
                } while(symbolMatcher.find());
            }            
        }

        inputReader.close();
        process.destroy();

        String[][] namesSymbols = {names.toArray(new String[0]), symbols.toArray(new String[0])};

        return namesSymbols;
    }

    /**
     * Function to search for a specific prefix
     * @param arr array of all stocks
     * @param prefix prefix to search for
     * @return If prefix not present, return insertion ind. Else, return earliest ind.
     */
    private static int binarySearch(String[] arr, String prefix) {
        int ind = Arrays.binarySearch(arr, prefix);

        if(ind < 0) {
            ind = -1*(ind + 1);
        }
        else {
            while((ind - 1 >= 0) && arr[ind - 1].equals(prefix))
                ind -= 1;
        }

        return ind;
    }

    /**
     * Function to sort arguments
     * @param arr array to sort
     * @return sorted array
     */
    private static int[] argSort(String[] arr) {
        Object[][] stringIndArray = new Object[arr.length][2];
        for(int i = 0; i < stringIndArray.length; i++) {
            stringIndArray[i][0] = arr[i];
            stringIndArray[i][1] = i;
        }
        Arrays.sort(stringIndArray, (a, b) -> ((String) (a[0])).compareTo((String) (b[0])));
        
        int[] sortedInds = new int[arr.length];
        for(int i = 0; i < arr.length; i++) {
            sortedInds[i] = (int) (stringIndArray[i][1]);
        }

        return sortedInds;
    }

}