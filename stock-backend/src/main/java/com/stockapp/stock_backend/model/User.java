package com.stockapp.stock_backend.model;

import com.google.gson.JsonArray;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Handles user-related operations (account + portfolio + transactions + watchlist)
 */
//@Data
public class User {
    private final static String projectId = "rice-comp-539-spring-2022";
    private final static String instanceId = "comp-539-bigtable";
    private final static BigtableManager bt;

    static {
        try {
            bt = new BigtableManager(projectId, instanceId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Re-calculate portfolio values every time we perform a new buy/sell action
     *
     * @param uid The user id whose portfolio value is being calculated
     * @return the updated portfolio value
     */
    public static double calcPortfolioVal(String uid) {
        Map<String, Investment> investments = getInvestments(uid);
        double total = 0;

        for (Map.Entry<String, Investment> entry : investments.entrySet()) {
            String symbol = entry.getKey();
            int shares = entry.getValue().getShares();
            try {
                double currentPrice = Stock.getCurrentPrice(symbol);
                total += shares * currentPrice;
            } catch (IOException e) {
                System.err.println("fail to fetch the stock price from api: " + e.getMessage());
            }
        }

        return total;
    }

    /**
     * Checks if a user exists in the database.
     *
     * @param uid User ID
     * @return true if user exists, false otherwise
     */
    public static Boolean userExists(String uid) {
        return bt.userExists(uid);
    }

    /**
     * Creates a new user in the database.
     *
     * @param uid      User ID
     * @param username User's username
     * @param email    User's email
     */
    public static void createUser(String uid, String username, String email) {
        bt.createUser(uid, username, email);
    }

    /**
     * Deletes the user.
     *
     * @param uid User ID
     */
    public static void deleteUser(String uid) {
        bt.deleteUser(uid);
    }

    /**
     * Resets a user by deleting and recreating the record with the same username and email.
     *
     * @param uid User ID
     */
    public static void resetUser(String uid) {
        String username = getUsername(uid);
        String email = getEmail(uid);

        deleteUser(uid);
        createUser(uid, username, email);
    }

    /**
     * Retrieves the user's username.
     *
     * @param uid User ID
     * @return Username
     */
    public static String getUsername(String uid) {
        return bt.getUsername(uid);
    }

    /**
     * Retrieves the user's email.
     *
     * @param uid User ID
     * @return Email address
     */
    public static String getEmail(String uid) {
        return bt.getEmail(uid);
    }

    /**
     * Retrieves and updates the latest portfolio value.
     *
     * @param uid User ID
     * @return Updated portfolio value
     */
    public static double getPortfolioValue(String uid) {
        // Re-calculate & update portfolio balance every time this function is called
        double portfolioVal = calcPortfolioVal(uid);
        setPortfolioValue(uid, portfolioVal);
//        System.out.println("RECALCULATING PORTFOLIO VALUE...");
//        System.out.println("calculated new portfolio value: " + portfolioVal);
        return bt.getPortfolioValue(uid);
    }

    /**
     * Updates the user's stored portfolio value.
     *
     * @param uid            User ID
     * @param portfolioValue New portfolio value
     */
    public static void setPortfolioValue(String uid, double portfolioValue) {
        bt.updatePortfolioValue(uid, portfolioValue);
    }

    /**
     * Retrieves the user's uninvested cash.
     *
     * @param uid User ID
     * @return Amount of uninvested cash
     */
    public static double getUninvested(String uid) {
        return bt.getUnivestedValue(uid);
    }

    /**
     * Updates the user's uninvested cash.
     *
     * @param uid        User ID
     * @param uninvested Amount of uninvested cash
     */
    public static void setUninvested(String uid, double uninvested) {
        bt.updateUninvestedValue(uid, uninvested);
    }

    /**
     * Retrieves the user's invested amount.
     *
     * @param uid User ID
     * @return Total invested value
     */
    public static double getInvested(String uid) {
        return bt.getIvestedValue(uid);
    }

    /**
     * Updates the user's invested amount.
     *
     * @param uid      User ID
     * @param invested New invested amount
     */
    public static void setInvested(String uid, double invested) {
        bt.updateInvestedValue(uid, invested);
    }

    /**
     * Retrieves the user's investment holdings.
     *
     * @param uid User ID
     * @return Map of stock symbol to Investment
     */
    public static Map<String, Investment> getInvestments(String uid) {
        return bt.getInvestments(uid);
    }

    /**
     * Updates the user's investment data.
     *
     * @param uid         User ID
     * @param investments Updated investment map
     */
    public static void updateInvestments(String uid, Map<String, Investment> investments) {
        bt.updateInvestments(uid, investments);
    }

    /**
     * Retrieves the user's transaction history.
     *
     * @param uid User ID
     * @return List of transactions
     */
    public static List<Transaction> getTransactionHistory(String uid) {
        return bt.getTransactions(uid);
    }

    /**
     * Updates the user's transaction history.
     *
     * @param uid          User ID
     * @param transactions List of transactions to save
     */
    public static void updateTransactions(String uid, List<Transaction> transactions) {
        bt.updateTransactions(uid, transactions);
    }

    /**
     * Buys stock and updates portfolio data.
     *
     * @param uid         User ID
     * @param stockSymbol Ticker symbol
     * @param quantity    Number of shares
     * @param stockPrice  Price per share
     * @return true if purchase is successful, false otherwise
     */
    public static boolean buyStock(String uid, String stockSymbol, int quantity, double stockPrice) {
        // Get current balances and investments
        double uninvested = getUninvested(uid);
        double invested = getInvested(uid);
        Map<String, Investment> investments = getInvestments(uid);
        List<Transaction> transactionHistory = getTransactionHistory(uid);
        double totalCost = quantity * stockPrice;

        // Check if the user has enough cash to buy
        if (uninvested >= totalCost) {
            uninvested -= totalCost;
            invested += totalCost;

            // Add new investment if not exist
            investments.putIfAbsent(stockSymbol, new Investment(stockSymbol, 0, 0));
            Investment investment = investments.get(stockSymbol);
            investment.incrementShares(quantity, stockPrice); // Update shares and average price

            // Add transaction history
            Transaction transaction = new Transaction(stockSymbol, quantity, stockPrice, new Date(), true);
            transactionHistory.add(transaction);

            // Save to database
            setUninvested(uid, uninvested);
            setInvested(uid, invested);
            updateInvestments(uid, investments);
            updateTransactions(uid, transactionHistory);

            // Re-calculate and update portfolio value
            double portfolioVal = calcPortfolioVal(uid);
            setPortfolioValue(uid, portfolioVal);

            return true;
        } else {
            return false; // Insufficient balance to buy stock
        }
    }

    /**
     * Sells stock and updates portfolio data.
     *
     * @param uid         User ID
     * @param stockSymbol Ticker symbol
     * @param quantity    Number of shares to sell
     * @param stockPrice  Price per share
     * @return true if sale is successful, false otherwise
     */
    public static boolean sellStock(String uid, String stockSymbol, int quantity, double stockPrice) {
        // Get current balances and investments
        double uninvested = getUninvested(uid);
        double invested = getInvested(uid);
        Map<String, Investment> investments = getInvestments(uid);
        List<Transaction> transactionHistory = getTransactionHistory(uid);

        // Check if current stock is in the portfolio
        if (investments.containsKey(stockSymbol)) {
            Investment investment = investments.get(stockSymbol);
            if (investment.getShares() >= quantity) {
                double totalSale = quantity * stockPrice;

                // Update investment & balance
                investment.decrementShares(quantity);
                if (investment.getShares() == 0) {
                    investments.remove(stockSymbol);
                }

                uninvested += totalSale;
                invested -= (investment.getAvgPrice() * quantity);

                // Update transaction history
                Transaction transaction = new Transaction(stockSymbol, quantity, stockPrice, new Date(), false);
                transactionHistory.add(transaction);

                // Save to database
                setUninvested(uid, uninvested);
                setInvested(uid, invested);
                updateInvestments(uid, investments);
                updateTransactions(uid, transactionHistory);

                // Re-calculate and update portfolio values
                double portfolioVal = calcPortfolioVal(uid);
                setPortfolioValue(uid, portfolioVal);

                return true;
            }
        }
        return false; // Stock not found in portfolio or not enough shares to sell
    }

    /**
     * Retrieves the user's stock watchlist.
     *
     * @param uid User ID
     * @return JSON array of stock symbols
     */
    public static JsonArray getWatchlist(String uid) {
        return bt.getWatchlist(uid);
    }

    /**
     * Checks if a stock is in the user's watchlist.
     *
     * @param uid         User ID
     * @param stockSymbol Ticker symbol
     * @return Index in watchlist, or -1 if not found
     */
    public static int inWatchlist(String uid, String stockSymbol) {
        JsonArray watchlist = getWatchlist(uid);
        for (int i = 0; i < watchlist.size(); i++) {
            if (watchlist.get(i).getAsString().equals(stockSymbol)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Adds a stock to the user's watchlist if not already present.
     *
     * @param uid         User ID
     * @param stockSymbol Ticker symbol to add
     */
    public static void addToWatchlist(String uid, String stockSymbol) {
        JsonArray watchlist = getWatchlist(uid);
//        System.out.println(watchlist.toString());
        int idx = inWatchlist(uid, stockSymbol);
        if (idx == -1) {
            watchlist.add(stockSymbol);
            bt.updateWatchlist(uid, watchlist);
        }
    }

    /**
     * Removes a stock from the user's watchlist.
     *
     * @param uid         User ID
     * @param stockSymbol Ticker symbol to remove
     */
    public static void removeFromWatchlist(String uid, String stockSymbol) {
        JsonArray watchlist = getWatchlist(uid);
        int idx = inWatchlist(uid, stockSymbol);
        if (idx != -1) {
            watchlist.remove(idx);
            bt.updateWatchlist(uid, watchlist);
        }
    }
}