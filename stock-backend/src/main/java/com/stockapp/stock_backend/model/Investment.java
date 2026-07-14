package com.stockapp.stock_backend.model;

import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Investment represents a user's investment in a specific stock.
 * It contains the stock symbol, number of shares owned, and the average price per share.
 * It provides methods to increment and decrement shares, adjusting the average price accordingly.
 */
@ToString
@EqualsAndHashCode
//@AllArgsConstructor
public class Investment {
    @Getter private final String stockSymbol;
    @Getter private int shares;
    @Getter private double avgPrice;


    /**
     * Constructs an Investment object.
     * Replaces @AllArgsConstructor due to test build issues.
     * (Lombok's annotation processing cannot be recognized during test compilation)
     *
     * @param stockSymbol the symbol of the stock (e.g., AAPL, MSFT)
     * @param shares the number of shares owned
     * @param avgPrice the average price per share
     */
    public Investment(String stockSymbol, int shares, double avgPrice) {
        this.stockSymbol = stockSymbol;
        this.shares = shares;
        this.avgPrice = avgPrice;
    }

    /**
     * Returns the stock symbol associated with this investment.
     *
     * @return the stock symbol
     */
    public String getStockSymbol() {
        return stockSymbol;
    }

    /**
     * Returns the number of shares currently owned.
     *
     * @return the number of shares
     */
    public int getShares() {
        return shares;
    }

    /**
     * Returns the average price per share for this investment.
     *
     * @return the average price per share
     */
    public double getAvgPrice() {
        return avgPrice;
    }

    /**
     * Increment shares of this investment and sets new avg price. This is used when buying shares.
     * @param shares the number of shares to increment
     * @param price the price at which the shares were bought
     */
    public void incrementShares(int shares, double price) {
        double new_price = ((this.shares * this.avgPrice) + (shares * price)) / (this.shares + shares);
        this.shares += shares;
        this.avgPrice = new_price;
    }

    /**
     * Decrement shares of this investment. This is used when selling shares.
     * @param shares the number of shares to decrement
     * @throws IndexOutOfBoundsException if shares would become negative.
     */
    public void decrementShares(int shares) throws IndexOutOfBoundsException {
        if ((this.shares - shares) < 0) {
            // should prob do something if shares would become negative
            throw new IndexOutOfBoundsException("Cannot decrease shares below zero.");
        }
        this.shares -= shares;
    }
}
