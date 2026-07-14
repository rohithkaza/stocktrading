package com.stockapp.stock_backend.model;

import java.util.Date;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Transaction represents a stock transaction made by a user.
 */
@ToString
@EqualsAndHashCode
//@AllArgsConstructor
public class Transaction {
    @Getter private final String stockSymbol;
    @Getter private final int shares;
    @Getter private final double price;
    @Getter private final Date date;
    @Getter private final boolean isBuy; // Buy or Sell // or make this a boolean?

    /**
     * Constructs a Transaction object.
     * Replaces @AllArgsConstructor due to test build issues.
     * (Lombok's annotation processing cannot be recognized during test compilation)
     *
     * @param stockSymbol the symbol of the stock being traded (e.g., TSLA, AMZN)
     * @param shares the number of shares bought or sold
     * @param price the price per share for the transaction
     * @param date the date and time when the transaction occurred
     * @param isBuy true if the transaction is a buy, false if it is a sell
     */
    public Transaction(String stockSymbol, int shares, double price, Date date, boolean isBuy) {
        this.stockSymbol = stockSymbol;
        this.shares = shares;
        this.price = price;
        this.date = date;
        this.isBuy = isBuy;
    }

    /**
     * Returns the stock symbol of the transaction.
     *
     * @return the stock symbol
     */
    public String getStockSymbol() {
        return stockSymbol;
    }

    /**
     * Returns the number of shares involved in the transaction.
     *
     * @return the number of shares
     */
    public int getShares() {
        return shares;
    }

    /**
     * Returns the price per share at the time of transaction.
     *
     * @return the transaction price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Returns the date and time the transaction occurred.
     *
     * @return the transaction date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Returns whether the transaction was a buy.
     *
     * @return true if it was a buy, false if it was a sell
     */
    public boolean isBuy() {
        return isBuy;
    }
}
