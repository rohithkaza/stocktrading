package com.stockapp.stock_backend.server;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.stockapp.stock_backend.model.Investment;
import com.stockapp.stock_backend.model.Stock;
import com.stockapp.stock_backend.model.Transaction;
import com.stockapp.stock_backend.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Service class that handles server-side logic for the stock application.
 * It interacts with Bigtable, manages user accounts, and processes stock transactions.
 */
@Service
public class ServerService {

    /**
     * HTTP client for sending messages.
     */
    final HttpClient CLIENT;
    private final Firestore firestore;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Timestamp formatter
     */
    DateTimeFormatter TSFORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");

    /**
     * No-arg server constructor.
     */
    public ServerService(Firestore firestore, BCryptPasswordEncoder passwordEncoder) {
        CLIENT = HttpClient.newHttpClient();
        this.firestore = firestore;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Respond to a user's request to access this resource with "Hello World!"
     *
     * @return Upon successful login, a brief message with a 200 OK status code is returned.
     */
    public ResponseEntity<String> helloWorld() {

        System.out.println("Log: saying 'Hello World' to client at " + timestamp());

        return ResponseEntity.ok()
                .body("Hello World!");
    }

    public ResponseEntity<String> addNewUser(String username, String email, String password) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("users").document(email);
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("password", passwordEncoder.encode(password));
        data.put("createdAt", timestamp());

        ApiFuture<WriteResult> result = docRef.set(data);

        System.out.println("Log: adding user " + username + " to Firestore at " + result.get().getUpdateTime());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("username", username);
        jsonObject.addProperty("createdAt", timestamp());

        return ResponseEntity.ok().body(jsonObject.toString());
    }

    /**
     * Signs up a user with the given uid, email, and username.
     * This method creates a new user in the database and returns a response indicating success or failure.
     *
     * @param uid      The unique identifier for the user.
     * @param email    The email address of the user.
     * @param username The username of the user.
     * @return A ResponseEntity containing a message indicating whether the user was added successfully or not.
     */
    public ResponseEntity<String> signup(String uid, String email, String username) {
        User.createUser(uid, email, username);
        if (User.getUsername(uid) == username) {
            return ResponseEntity.ok().body("User added");
        } else {
            return ResponseEntity.badRequest().body("User could not be added");
        }
    }


    /**
     * Buy stock and update user data.
     *
     * @param uid         The user ID of the user making the purchase.
     * @param stockSymbol The stock symbol of the stock to be purchased.
     * @param quantity    The number of shares to be purchased.
     * @return ResponseEntity indicating success or failure of the purchase.
     * @throws ExecutionException   If an error occurs while executing the operation.
     * @throws InterruptedException If the operation is interrupted.
     */
    public ResponseEntity<String> buyStock(String uid, String stockSymbol, int quantity) throws ExecutionException, InterruptedException {
        double stockPrice = 0;
//        try {
//            boolean isOpen = Stock.isMarketOpen();
//            if (!isOpen) {
//                return ResponseEntity.badRequest().body("Market is not open");
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        try {
            stockPrice = Stock.getCurrentPrice(stockSymbol);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        boolean success = User.buyStock(uid, stockSymbol, quantity, stockPrice);
        if (!success) {
            return ResponseEntity.badRequest().body("Insufficient funds to buy stock");
        }
        return ResponseEntity.ok().body("Stock bought successfully");
    }


    /**
     * Sells a specified quantity of stock for a user and updates their data.
     * Validates the user's stock holdings and returns an appropriate response.
     *
     * @param uid         The user ID of the user selling the stock.
     * @param stockSymbol The stock symbol of the stock to be sold.
     * @param quantity    The number of shares to be sold.
     * @return ResponseEntity indicating success or failure of the sale.
     * @throws ExecutionException   If an error occurs while executing the operation.
     * @throws InterruptedException If the operation is interrupted.
     */
    public ResponseEntity<String> sellStock(String uid, String stockSymbol, int quantity) throws ExecutionException, InterruptedException {
        double stockPrice = 0;
        try {
            stockPrice = Stock.getCurrentPrice(stockSymbol);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        boolean success = User.sellStock(uid, stockSymbol, quantity, stockPrice);
        if (!success) {
            return ResponseEntity.badRequest().body("Not enough shares to sell");
        }
        return ResponseEntity.ok().body("Stock sold successfully");
    }

    public ResponseEntity<String> login(String email, String password) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("users").document(email);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot documentSnapshot = future.get();
        if (documentSnapshot.exists()) {
            if (passwordEncoder.matches(password, documentSnapshot.getString("password"))) {
                return ResponseEntity.ok().body("Successfully logged in");
            }
        }
        return ResponseEntity.notFound().build();


    }

    /**
     * Returns the company description for a given stock symbol.
     *
     * @param stockSymbol The stock symbol of the company.
     * @return A ResponseEntity containing a JSON object with the company description.
     * @throws ExecutionException   If an error occurs while executing the operation.
     * @throws InterruptedException If the operation is interrupted.
     */
    public ResponseEntity<JsonObject> getCompanyDescription(String stockSymbol) {
        
        JsonObject data = new JsonObject();
        String[] wikiOutput = Stock.getWikiCompanyDescription(stockSymbol);
        if(wikiOutput.length > 0) {
            data.addProperty("sourceType", "Wikipedia");
            data.addProperty("url", wikiOutput[0]);
            data.addProperty("description", wikiOutput[2]);
            data.addProperty("articleTitle", wikiOutput[1]);
            JsonArray citationNums = new JsonArray();
            for(int i = 3; i < wikiOutput.length; i+=2)
                citationNums.add(wikiOutput[i]);
            JsonArray citationTexts = new JsonArray();
            for(int i = 4; i < wikiOutput.length; i+=2)
                citationTexts.add(wikiOutput[i]);
            data.add("citationNums", citationNums);
            data.add("citationTexts", citationTexts);
        } else {
            String[] avOutput = Stock.getAlphaVantageCompanyDescription(stockSymbol);
            //System.out.println(avOutput[0]);
            if(avOutput[1].equals("0")) {
                data.addProperty("description", avOutput[0]);
            } else if(avOutput[1].equals("1")) {
                data.addProperty("description", "No Company Information Available. Please try a Different Stock.");
            } else if(avOutput[1].equals("2")) {
                data.addProperty("description", "API Rate Limit Reached. Please Try Again in 24 Hours. Or, Try a Different Stock.");
            } else { // "3"
                data.addProperty("description", "Internal API Error. Please Try Again Later. Or, Try a Different Stock.");
            }
            data.addProperty("sourceType", "AlphaVantage");
            data.addProperty("url", "");
            data.addProperty("articleTitle", "");
            data.add("citationNums", new JsonArray());
            data.add("citationTexts", new JsonArray());
        }
        return ResponseEntity.ok().body(data);
    }

    /**
     * Fetches historical stock data for a given stock symbol and timeframe.
     *
     * @param stockSymbol The stock symbol to fetch data for.
     * @param timeframe   The timeframe for the historical data
     * @param startDate   The start date for the historical data in "YYYY-MM-DD" format.
     * @param endDate     The end date for the historical data in "YYYY-MM-DD" format.
     * @return A ResponseEntity containing the historical stock data as a JSON object or error message.
     * @throws ExecutionException   If an error occurs while executing the operation.
     * @throws InterruptedException If the operation is interrupted.
     */
    public ResponseEntity<JsonObject> getHistoricalData(String stockSymbol, String timeframe, String startDate, String endDate) throws ExecutionException, InterruptedException {
        try {
            JsonObject data = Stock.getHistoricalPrices(stockSymbol, timeframe, startDate, endDate);
//            System.out.println(data);
            return ResponseEntity.ok().body(data);
        } catch (IOException e) {
//            System.out.println(e.getMessage());
            String message = e.getMessage();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", message);
            return ResponseEntity.badRequest().body(jsonObject);
        }
    }

    /**
     * Fetches the current stock price for a given stock symbol.
     *
     * @param stockSymbol The stock symbol to fetch the current price for.
     * @return A ResponseEntity containing the current stock price as a JSON object or error message.
     * @throws ExecutionException   If an error occurs while executing the operation.
     * @throws InterruptedException If the operation is interrupted.
     */
    public ResponseEntity<JsonObject> getCurrentPrice(String stockSymbol) throws ExecutionException, InterruptedException {
        try {
            float price = Stock.getCurrentPrice(stockSymbol);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("price", price);
            return ResponseEntity.ok().body(jsonObject);
        } catch (IOException e) {
            String message = e.getMessage();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", message);
            return ResponseEntity.badRequest().body(jsonObject);
        }
    }

    /**
     * Fetches the change (change between current and previous closing price) in stock price for a given stock symbol.
     *
     * @param stockSymbol The stock symbol to fetch the change for.
     * @return A ResponseEntity containing the absolute change as a JSON object or error message.
     * @throws ExecutionException   If an error occurs while executing the operation.
     * @throws InterruptedException If the operation is interrupted.
     */
    public ResponseEntity<JsonObject> getChange(String stockSymbol) throws ExecutionException, InterruptedException {
        try {
            JsonObject snapshotData = Stock.getSnapshot(stockSymbol);
            float currClose = snapshotData.get("dailyBar").getAsJsonObject().get("c").getAsFloat();
            float prevClose = snapshotData.get("prevDailyBar").getAsJsonObject().get("c").getAsFloat();
            float change = currClose - prevClose;

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("change", change);
            return ResponseEntity.ok().body(jsonObject);
        } catch (IOException e) {
            String message = e.getMessage();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", message);
            return ResponseEntity.badRequest().body(jsonObject);
        }
    }

    /**
     * Fetches the percentage change in stock price for a given stock symbol.
     *
     * @param stockSymbol The stock symbol to fetch the percentage change for.
     * @return A ResponseEntity containing the percentage change as a JSON object or error message.
     * @throws ExecutionException   If an error occurs while executing the operation.
     * @throws InterruptedException If the operation is interrupted.
     */
    public ResponseEntity<JsonObject> getChangePercent(String stockSymbol) throws ExecutionException, InterruptedException {
        try {
            JsonObject snapshotData = Stock.getSnapshot(stockSymbol);
            float currClose = snapshotData.get("dailyBar").getAsJsonObject().get("c").getAsFloat();
            float prevClose = snapshotData.get("prevDailyBar").getAsJsonObject().get("c").getAsFloat();
            float change = currClose - prevClose;
            float changePercent = (change / prevClose) * 100;

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("changePercent", changePercent);
            return ResponseEntity.ok().body(jsonObject);
        } catch (IOException e) {
            String message = e.getMessage();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", message);
            return ResponseEntity.badRequest().body(jsonObject);
        }
    }

    /**
     * Calculates the total and percent changes in the user's portfolio value
     *
     * @param uid The user id whose portfolio change is being calculated
     * @return A ResponseEntity containing a map of the total value change + percentage change
     * Returns a 400 Bad Request if snapshot fetching fails for any stock
     */
    public ResponseEntity<Map<String, Double>> getPortfolioChange(String uid) throws ExecutionException, InterruptedException {
        Map<String, Investment> investments = User.getInvestments(uid);
        double totalPrev = 0;
        double totalCurr = 0;

        // Springboot cannot serialize gson.jsonObject
        // so i'm using map for now
        Map<String, Double> result = new HashMap<>();

        // Loop through all stocks and get their price changes
        for (Map.Entry<String, Investment> entry : investments.entrySet()) {
            String symbol = entry.getKey();
            int shares = entry.getValue().getShares();
            try {
                // Get current & previous close prices
                JsonObject snapshot = Stock.getSnapshot(symbol);
                double currClose = snapshot.get("dailyBar").getAsJsonObject().get("c").getAsDouble();
                double prevClose = snapshot.get("prevDailyBar").getAsJsonObject().get("c").getAsDouble();
                totalCurr += currClose * shares;
                totalPrev += prevClose * shares;
            } catch (IOException e) {
                System.out.println("fail to fetch snapshot for " + symbol);
                return ResponseEntity.badRequest().body(result);
            }
        }
        // Calculate absolute & percentage changes
        double change = totalCurr - totalPrev;
        double percent = 0;
        if (totalPrev != 0) {
            percent = (change / totalPrev) * 100;
        }
        // Populate result map
        result.put("change", change);
        result.put("percentChange", percent);
        return ResponseEntity.ok(result);
    }

    /**
     * Retrieves the total value of the user's portfolio.
     *
     * @param user The user ID whose portfolio value is being retrieved.
     * @return A ResponseEntity containing the portfolio value as a JSON object.
     * @throws ExecutionException   If an error occurs while executing the operation.
     * @throws InterruptedException If the operation is interrupted.
     */
    public ResponseEntity<JsonObject> getPortfolioValue(String user) throws ExecutionException, InterruptedException {
        double balance = User.getPortfolioValue(user);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("balance", balance);
        return ResponseEntity.ok().body(jsonObject);
    }

    /**
     * Retrieves the uninvested cash balance of the user.
     *
     * @param user The user ID whose uninvested balance is being retrieved.
     * @return A ResponseEntity containing the uninvested balance as a JSON object.
     * @throws ExecutionException   If an error occurs while executing the operation.
     * @throws InterruptedException If the operation is interrupted.
     */
    public ResponseEntity<JsonObject> getUninvested(String user) throws ExecutionException, InterruptedException {
        double balance = User.getUninvested(user);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("balance", balance);
        return ResponseEntity.ok().body(jsonObject);
    }


    public ResponseEntity<JsonObject> getInvested(String user) throws ExecutionException, InterruptedException {
        double balance = User.getInvested(user);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("balance", balance);
        return ResponseEntity.ok().body(jsonObject);
    }

    /**
     * Retrieves the number of shares owned by the user for a given stock symbol.
     *
     * @param user        The user ID whose stock shares are being retrieved.
     * @param stockSymbol The stock symbol to check for shares.
     * @return A ResponseEntity containing the number of shares as a JSON object.
     * @throws ExecutionException   If an error occurs while executing the operation.
     * @throws InterruptedException If the operation is interrupted.
     */
    public ResponseEntity<JsonObject> getStockShares(String user, String stockSymbol) throws ExecutionException, InterruptedException {
        Map<String, Investment> investments = User.getInvestments(user);
        Investment investment = investments.get(stockSymbol);
        int shares;
        if (investment != null) {
            shares = investment.getShares();
        } else {
            shares = 0;
        }
//        int shares = 1; // dummy

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("shares", shares);
        return ResponseEntity.ok().body(jsonObject);
    }

    /**
     * Retrieves the user's investments.
     *
     * @param user The user ID whose investments are being retrieved.
     * @return A ResponseEntity containing the user's investments as a string.
     * @throws ExecutionException   If an error occurs while executing the operation.
     * @throws InterruptedException If the operation is interrupted.
     */
    public ResponseEntity<String> getInvestments(String user) throws ExecutionException, InterruptedException {
        Map<String, Investment> investments = User.getInvestments(user);
        Gson gson = new Gson();
        String newInvestments = gson.toJson(investments);
        return ResponseEntity.ok().body(newInvestments);
    }

    /**
     * Retrieves the transaction history of a user.
     *
     * @param uid The user ID whose transaction history is being retrieved.
     * @return A ResponseEntity containing the transaction history as a JSON array.
     */
    public ResponseEntity<String> getTransactionHistory(String uid) {
        List<Transaction> transactions = User.getTransactionHistory(uid);
        transactions.sort((a, b) -> b.getDate().compareTo(a.getDate()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        JsonArray jsonArray = new JsonArray();

        for (Transaction tx : transactions) {
            JsonObject obj = new JsonObject();

            obj.addProperty("symbol", tx.getStockSymbol());
            obj.addProperty("action", tx.isBuy() ? "Buy" : "Sell");
            obj.addProperty("price", String.format("$%.2f", tx.getPrice()));
            obj.addProperty("shares", tx.getShares());
            obj.addProperty("total", String.format("$%.2f", tx.getPrice() * tx.getShares()));
            obj.addProperty("date", dateFormat.format(tx.getDate()));
            obj.addProperty("time", timeFormat.format(tx.getDate()));

            jsonArray.add(obj);
        }

        return ResponseEntity.ok().body(jsonArray.toString());
    }

    /**
     * Retrieves recent news articles related to a stock.
     *
     * @param stockSymbol The stock symbol to fetch news for.
     * @param startDate   The start date for the news articles in "YYYY-MM-DD" format.
     * @param endDate     The end date for the news articles in "YYYY-MM-DD" format.
     * @param limit       The maximum number of news articles to retrieve.
     * @return A ResponseEntity containing the news data as a JSON array or error message.
     * @throws ExecutionException   If an error occurs while executing the operation.
     * @throws InterruptedException If the operation is interrupted.
     */
    public ResponseEntity<JsonArray> getNews(String stockSymbol, String startDate, String endDate, int limit) throws ExecutionException, InterruptedException {
        JsonArray newsData;
        try {
            newsData = Stock.getNews(stockSymbol, startDate, endDate, limit);
            return ResponseEntity.ok().body(newsData);
        } catch (IOException e) {
            System.out.println("fail to fetch news for " + stockSymbol);
            String message = e.getMessage();
            JsonArray arr = new JsonArray();
            arr.add(message);
            return ResponseEntity.badRequest().body(arr);
        }
    }

    /**
     * Adds a stock to the user's watchlist.
     *
     * @param uid         The user ID of the user adding the stock.
     * @param stockSymbol The stock symbol to be added to the watchlist.
     * @return A ResponseEntity indicating success or failure of the operation.
     */
    public ResponseEntity<String> addToWatchlist(String uid, String stockSymbol) {
        try {
            User.addToWatchlist(uid, stockSymbol);
            JsonObject response = new JsonObject();
            response.addProperty("message", "success");
            return ResponseEntity.ok().body(response.toString());
        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("message", "unable to add to watchlist");
            return ResponseEntity.badRequest().body(response.toString());
        }
    }

    /**
     * Removes a stock from the user's watchlist.
     *
     * @param uid         The user ID of the user removing the stock.
     * @param stockSymbol The stock symbol to be removed from the watchlist.
     * @return A ResponseEntity indicating success or failure of the operation.
     */
    public ResponseEntity<String> removeFromWatchlist(String uid, String stockSymbol) {
        try {
            User.removeFromWatchlist(uid, stockSymbol);
            JsonObject response = new JsonObject();
            response.addProperty("message", "success");
            return ResponseEntity.ok().body(response.toString());
        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("message", "unable to remove from watchlist");
            return ResponseEntity.badRequest().body(response.toString());
        }
    }

    /**
     * Retrieves the user's watchlist.
     *
     * @param uid The user ID whose watchlist is being retrieved.
     * @return A ResponseEntity containing the watchlist as a JSON array.
     */
    public ResponseEntity<JsonArray> getWatchlist(String uid) {
        //TODO: error handling for watchlist
        JsonArray watchlist = User.getWatchlist(uid);
        return ResponseEntity.ok().body(watchlist);
    }

    /**
     * Checks if a stock is in the user's watchlist.
     *
     * @param uid         The user ID of the user checking the watchlist.
     * @param stockSymbol The stock symbol to check for in the watchlist.
     * @return A ResponseEntity indicating whether the stock is in the watchlist (true/false).
     */
    public ResponseEntity<Boolean> inWatchlist(String uid, String stockSymbol) {
        int watchlistId = User.inWatchlist(uid, stockSymbol);
        if (watchlistId > 0) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok(false);
        }
    }

    /**
     * Retrieves the industry breakdown of the user's investments.
     *
     * @param uid The user ID whose industry breakdown is being retrieved.
     * @return A ResponseEntity containing a JSON object with the industry breakdown.
     */
    public ResponseEntity<JsonObject> getIndustryBreakdown(String uid) {
        Map<String, Investment> investments = User.getInvestments(uid);
        System.out.println("get industry breakdown in server service: " + investments.toString());
        JsonObject industries = new JsonObject();

        for (Investment inv : investments.values()) {
            String stockSymbol = inv.getStockSymbol();
            double avgPrice = inv.getAvgPrice();
            int shares = inv.getShares();
            double total = avgPrice * shares;
            try {
                String industry = Stock.getIndustry(stockSymbol);
                if (industries.has(industry)) {
                    double count = industries.get(industry).getAsDouble();
                    industries.addProperty(industry, count + total);
                } else {
                    industries.addProperty(industry, total);
                }
            } catch (IOException e) {
                if (e.getMessage().equals("No finnhub industry")) { //stocks that do not exist
                    String industry = "Unknown";
                    if (industries.has(industry)) {
                        double count = industries.get(industry).getAsDouble();
                        industries.addProperty(industry, count + total);
                    } else {
                        industries.addProperty(industry, total);
                    }
                    continue;
                }
                System.out.println("fail to fetch industry for " + stockSymbol);
                String message = e.getMessage();
                JsonObject m = new JsonObject();
                m.addProperty("message", message);
                return ResponseEntity.badRequest().body(m);
            }
        }
        System.out.println("get industries breakdown in server service: " + industries.toString());
        return ResponseEntity.ok().body(industries);
    }

    /**
     * Resets the user's account data.
     *
     * @param uid The user ID of the user whose account is being reset.
     * @return A ResponseEntity indicating success or failure of the reset operation.
     */
    public ResponseEntity<String> resetAccount(String uid) {
        // TODO: error handling
        User.resetUser(uid);
        JsonObject response = new JsonObject();
        response.addProperty("message", "success");
        return ResponseEntity.ok().body(response.toString());
    }


    /**
     * Make UTC timestamp string for the current moment.
     *
     * @return A UTC timestamp string.
     */
    private String timestamp() {
        return ZonedDateTime.now(ZoneOffset.UTC).format(TSFORMATTER);
    }
}
