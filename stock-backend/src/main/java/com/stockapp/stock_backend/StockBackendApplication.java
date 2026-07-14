package com.stockapp.stock_backend;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.stockapp.stock_backend.model.Transaction;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import com.stockapp.stock_backend.model.Stock;
import com.stockapp.stock_backend.model.User;



@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class StockBackendApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(StockBackendApplication.class, args);
		System.out.println("Yay main works");
		System.out.println(Stock.getCurrentPrice("AAPL")+"\n");
		System.out.println(Stock.getHistoricalPrices("AAPL", "1Day", "2024-01-01", "2024-02-27")+"\n");
		System.out.println(Stock.getSnapshot("AAPL")+"\n");
		System.out.println(Stock.getNews("AAPL", "2024-02-27", "2024-02-28", 5)+"\n");

//		User.addToWatchlist("1tmOoiRHkPRicYTOPumrx4bbCC53", "TSLA");


//		System.out.println("=== START TEST ===");

		// 1. Create user and simulate transactions

//		User.createUser("demouser", "demousername", "demo@email.com");
//		User.updateTransactions("demouser", new ArrayList<>());
//		User.buyStock("demouser", "AAPL", 2, 150.0);
//		User.sellStock("demouser", "AAPL", 1, 155.0);
//		User.buyStock("demouser", "TSLA", 3, 700.0);


//		// 2. Get transaction history
//
//		List<Transaction> transactions = User.getTransactionHistory("demouser");
//		transactions.sort((a, b) -> b.getDate().compareTo(a.getDate())); // sort by date,
//		// descending
//
//
//		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
//		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
//		JsonArray formatted = new JsonArray();
//
//		for (Transaction tx : transactions) {
//			JsonObject obj = new JsonObject();
//			obj.addProperty("symbol", tx.getStockSymbol());
//			obj.addProperty("action", tx.isBuy() ? "Buy" : "Sell");
//			obj.addProperty("price", String.format("$%.2f", tx.getPrice()));
//			obj.addProperty("shares", tx.getShares());
//			obj.addProperty("total", String.format("$%.2f", tx.getPrice() * tx.getShares()));
//			obj.addProperty("date", dateFormat.format(tx.getDate()));
//			obj.addProperty("time", timeFormat.format(tx.getDate()));
//			formatted.add(obj);
//		}
//
//		System.out.println("\n--- Loaded + Formatted Transactions ---");
//		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
//		System.out.println(prettyGson.toJson(formatted));
//
//		System.out.println("=== END TEST ===");

	}

}