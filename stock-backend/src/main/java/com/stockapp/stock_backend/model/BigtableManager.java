package com.stockapp.stock_backend.model;

import com.google.gson.*;
import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.models.Row;
import com.google.cloud.bigtable.data.v2.models.RowMutation;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * BigtableManager is responsible for managing user data in Google Cloud Bigtable.
 * It provides methods to create, read, update, and delete user information,
 * including investments, transactions, and watchlists.
 */
public class BigtableManager {

    private final String usersTableID = "StockA-Info";
    private final BigtableDataClient client;
    private final Random RANDOM = new Random(0xC413 + Instant.now().getEpochSecond());

    public BigtableManager(String projectId, String instanceId) throws IOException {
        this.client = BigtableDataClient.create(projectId, instanceId);
    }

    void close() {
        this.client.close();
    }

    /**
     * Get a user by UID from Bigtable.
     */
//    public User getUserByUID(String uid) {
//        Row row = client.readRow(usersTableID, uid);
//        if (row == null) {
//            System.out.println("No user found with UID: " + uid);
//            return null;
//        }
//
//        return new User(
//                uid,  // Row key
//                row.getCells("Users", "username").get(0).getValue().toStringUtf8(),
//                row.getCells("Users", "email").get(0).getValue().toStringUtf8(),
//                Double.parseDouble(row.getCells("Users", "portfolioValue").get(0).getValue().toStringUtf8()),
//                Double.parseDouble(row.getCells("Users", "uninvested").get(0).getValue().toStringUtf8()),
//                Double.parseDouble(row.getCells("Users", "invested").get(0).getValue().toStringUtf8()),
//                row.getCells("Users", "investments").get(0).getValue().toStringUtf8(),
//                row.getCells("Users", "transactionHistory").get(0).getValue().toStringUtf8()
//        );
//    }

    /**
     * Gets the row in the database that corresponds to the user with uid.
     *
     * @param uid the user to retrieve.
     * @return Row from BigTable containing all the user's stored information; null if the user does not exist.
     */
    private Row getRowByUID(String uid) {
        Row row = client.readRow(usersTableID, uid);
        if (row == null) {
            System.out.println("No user found with UID: " + uid);
            return null;
        }
        return row;
    }

    /**
     * Determines if the user exists in the database.
     *
     * @param uid the user.
     * @return true if the user with uid exists, false otherwise.
     */
    public boolean userExists(String uid) {
        return client.readRow(usersTableID, uid) != null;
    }

    /**
     * Puts a new user in the database and populates that entry with default starting values.
     *
     * @param uid the user.
     * @param username the user's username.
     * @param email the user's email.
     */
    public void createUser(String uid, String username, String email) {
        // Prevent duplicate entries
        if (client.readRow(usersTableID, uid) != null) {
            System.out.println("User with UID \"" + uid + "\" already exists.");
            return;
        }

        Gson gson = new Gson();
        JsonObject investments = new JsonObject();
        JsonArray transactions = new JsonArray();
        JsonArray watchlist = new JsonArray();

        RowMutation mutation = RowMutation.create(usersTableID, uid)
                .setCell("Users", "username", username)
                .setCell("Users", "email", email)
                .setCell("Users", "portfolioValue", String.valueOf(0)) // start with 0 (no investments)
                .setCell("Users", "uninvested", String.valueOf(100000)) // start with 100k
                .setCell("Users", "invested", String.valueOf(0)) // nothing invested at start
                .setCell("Users", "investments", gson.toJson(investments)) // Convert object to string
                .setCell("Users", "transactionHistory", gson.toJson(transactions)) // Convert array to string
                .setCell("Users", "watchlist", gson.toJson(watchlist));  // Convert array to string, empty array to start
        client.mutateRow(mutation);
        System.out.println("User " + username + "(UID:" + uid + ") saved successfully.");
    }


    /**
     * Removes a user from the database.
     *
     * @param uid the user.
     */
    public void deleteUser(String uid) {
        Row row = client.readRow(usersTableID, uid);
        if (row == null) {
            System.out.println("❌ User with UID \"" + uid + "\" not found.");
            return;
        }

        client.mutateRow(RowMutation.create(usersTableID, uid).deleteRow());
        System.out.println("✅ User with UID \"" + uid + "\" deleted successfully.");
    }

    /**
     * Gets the username of a user.
     *
     * @param uid the user.
     * @return a String, the user's username; null if the user does not exist.
     */
    public String getUsername(String uid) {
        Row row = getRowByUID(uid);
        if (row == null) return null;
        return row.getCells("Users", "username").get(0).getValue().toStringUtf8();
    }

    /**
     * Gets the email of a user.
     *
     * @param uid the user.
     * @return a String, the user's email; null if the user does not exist.
     */
    public String getEmail(String uid) {
        Row row = getRowByUID(uid);
        if (row == null) return null;
        return row.getCells("Users", "email").get(0).getValue().toStringUtf8();
    }

    /**
     * Gets the portfolio value of a user.
     *
     * @param uid the user.
     * @return a number representing the user's portfolio balance; -1 if the user does not exist.
     */
    public double getPortfolioValue(String uid) {
        Row row = getRowByUID(uid);
        if (row == null) return -1;
        return Double.parseDouble(row.getCells("Users", "portfolioValue").get(0).getValue().toStringUtf8());
    }

    /**
     * Updates the user's portfolio balance.
     *
     * @param uid the user.
     * @param portfolioValue the new value of the user's portfolio.
     */
    public void updatePortfolioValue(String uid, double portfolioValue) {
        RowMutation mutation = RowMutation.create(usersTableID, uid).setCell("Users", "portfolioValue", String.valueOf(portfolioValue));
        client.mutateRow(mutation);
    }

    /**
     * Gets the amount the user has uninvested.
     *
     * @param uid the user.
     * @return a number, the amount of money the user has uninvested; -1 if the user does not exist.
     */
    public double getUnivestedValue(String uid) {
        Row row = getRowByUID(uid);
        if (row == null) return -1;
        return Double.parseDouble(row.getCells("Users", "uninvested").get(0).getValue().toStringUtf8());
    }

    /**
     * Updates the amount the user has uninvested.
     *
     * @param uid the user.
     * @param uninvestedValue the new amount the user has uninvested.
     */
    public void updateUninvestedValue(String uid, double uninvestedValue) {
        RowMutation mutation = RowMutation.create(usersTableID, uid).setCell("Users", "uninvested", String.valueOf(uninvestedValue));
        client.mutateRow(mutation);
    }

    /**
     * Gets the amount the user has invested.
     *
     * @param uid the user.
     * @return a number, tne amount of money the user has invested; -1 if the user does not exist.
     */
    public double getIvestedValue(String uid) {
        Row row = getRowByUID(uid);
        if (row == null) return -1;
        return Double.parseDouble(row.getCells("Users", "invested").get(0).getValue().toStringUtf8());
    }

    /**
     * Updates the amount the user has invested.
     *
     * @param uid the user.
     * @param investedValue the new amount the user has invested.
     */
    public void updateInvestedValue(String uid, double investedValue) {
        RowMutation mutation = RowMutation.create(usersTableID, uid).setCell("Users", "invested", String.valueOf(investedValue));
        client.mutateRow(mutation);
    }

    /**
     * Gets the investments the user currently has. Investments are stored as an JSON object that contains the stock name,
     * average price, and number of shares owned.
     *
     * @param uid the user.
     * @return a mapping between the company name of stocks invested in, and Investment objects, which store the stock
     * name, average price, and number of shares owned.
     */
    public Map<String, Investment> getInvestments(String uid) {
        Gson gson = new Gson();

        Row row = getRowByUID(uid);
        if (row == null) return null;
        String rawInvestments = row.getCells("Users", "investments").get(0).getValue().toStringUtf8();
        JsonObject JSONInvestments = gson.fromJson(rawInvestments, JsonObject.class);

        Map<String, Investment> investments = new HashMap<>();

        System.out.println(JSONInvestments);

        // convert JSON into map
        for (Map.Entry<String, JsonElement> entry : JSONInvestments.entrySet()) {
            String stockSymbol = entry.getKey();
            JsonObject stockData = entry.getValue().getAsJsonObject();

            System.out.println(stockSymbol);
            System.out.println(stockData);

            int shares = stockData.get("shares").getAsInt();
            double avgPrice = stockData.get("avgPrice").getAsDouble();

            investments.put(stockSymbol, new Investment(stockSymbol, shares, avgPrice));
        }

        return investments;
    }

    /**
     * Updates the user's investments.
     *
     * @param uid the user.
     * @param investments the user's new current investments.
     */
    public void updateInvestments(String uid, Map<String, Investment> investments) {
        Gson gson = new Gson();
        String newInvestments = gson.toJson(investments);

        RowMutation mutation = RowMutation.create(usersTableID, uid).setCell("Users", "investments", newInvestments);
        client.mutateRow(mutation);
    }

    /**
     * Gets the user's transaction history, which is stored as a JSON array containing the stock name, if the action
     * was a buy or sell, the number of shares involved in the transaction, the price bought/sold at, and the date of
     * the transaction.
     *
     * @param uid the user.
     * @return a list of Transaction objects, containing the information for all transactions made by the user.
     */
    public List<Transaction> getTransactions(String uid) {
        Gson gson = new Gson();

        Row row = getRowByUID(uid);
        if (row == null) return null;
        String rawTransactions = row.getCells("Users", "transactionHistory").get(0).getValue().toStringUtf8();
        JsonArray JSONTransactions = gson.fromJson(rawTransactions, JsonArray.class);

        List<Transaction> transactions = new ArrayList<>();

        // Convert to list
        for (JsonElement element : JSONTransactions) {
            JsonObject transactionObj = element.getAsJsonObject();

            String stockSymbol = transactionObj.get("stockSymbol").getAsString();
            int shares = transactionObj.get("shares").getAsInt();
            double price = transactionObj.get("price").getAsDouble();
            boolean isBuy = transactionObj.get("isBuy").getAsBoolean();
            //New way to save date
            Date date;
            try {
                //Get the date info and seperate from time info
                String dateString = transactionObj.get("date").getAsString();
                date = formatter.parse(dateString);
            } catch (Exception e) {
                System.out.println("Error parsing transaction date, using current time.");
                date = new Date();
            }

            transactions.add(new Transaction(stockSymbol, shares, price, date, isBuy));
        }

        return transactions;
    }

    /**
     * Update the user's transaction history.
     *
     * @param uid the user.
     * @param transactions the user's new current transaction history.
     */
    public void updateTransactions(String uid, List<Transaction> transactions) {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        String newTransactions = gson.toJson(transactions);
        RowMutation mutation = RowMutation.create(usersTableID, uid)
                .setCell("Users", "transactionHistory", newTransactions);
        client.mutateRow(mutation);
    }

    /**
     * Formats the date
     */
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    static {
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Gets the users current watchlist; the stocks the user is interested in monitoring but is not necessarily invested
     * in.
     *
     * @param uid the user.
     * @return a JSON array containing the tickers of all the stocks on the user's watchlist.
     */
    public JsonArray getWatchlist(String uid) {
        Gson gson = new Gson();

        Row row = getRowByUID(uid);
        if (row == null) return null;
        try {
            String rawWatchlist = row.getCells("Users", "watchlist").get(0).getValue().toStringUtf8();
            return gson.fromJson(rawWatchlist, JsonArray.class);
        } catch (ArrayIndexOutOfBoundsException e) { // if the user does not have a "watchlist", create an empty one
            JsonArray watchlist = new JsonArray();
            RowMutation mutation = RowMutation.create(usersTableID, uid).setCell("Users", "watchlist", gson.toJson(watchlist));
            client.mutateRow(mutation);
            return new JsonArray();
        }
    }

    /**
     * Updates the user's watchlist.
     *
     * @param uid the user.
     * @param watchlist the user's new current watchlist.
     */
    public void updateWatchlist(String uid, JsonArray watchlist) {
        Gson gson = new Gson();

        String newWatchlist = gson.toJson(watchlist);
        RowMutation mutation = RowMutation.create(usersTableID, uid).setCell("Users", "watchlist", newWatchlist);
        client.mutateRow(mutation);
    }

}