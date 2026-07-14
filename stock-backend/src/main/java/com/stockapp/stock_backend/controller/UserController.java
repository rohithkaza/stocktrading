package com.stockapp.stock_backend.controller;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
//import com.stockapp.stock_backend.model.User;
import com.stockapp.stock_backend.server.ServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Handles user-related API endpoints including portfolio, investments, watchlist,
 * transaction history, and stock transactions (buy/sell).
 */


@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * Service that performs the business logic of request handling - "The Server".
     */
    private final ServerService serverService;

    /**
     * Constructor for API Controller that passes requests to a server instance.
     * @param serverService     Instance of server handling business logic of requests.
     */
    @Autowired
    public UserController(ServerService serverService) {
        this.serverService = serverService;
    }

    /**
     * Signs up a user (alternative to auth controller signup).
     */

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestAttribute("uid") String uid, @RequestAttribute("email") String email) {

        return serverService.signup(uid, email, "username");
    }

    /**
     * Returns total value of the user's portfolio.
     */

    @PostMapping("/portfolioval")
    public ResponseEntity<String> getPortfolioValue(@RequestAttribute("uid") String uid)
            throws ExecutionException, InterruptedException {

        ResponseEntity<JsonObject> response = serverService.getPortfolioValue(uid);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody().toString());
    }
    /**
     * Returns the uninvested (cash) balance in the user's account.
     */

    @PostMapping("/uninvested")
    public ResponseEntity<String> getUninvested(@RequestAttribute("uid") String uid)
            throws ExecutionException, InterruptedException {

        ResponseEntity<JsonObject> response = serverService.getUninvested(uid);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody().toString());
    }

    /**
     * Returns the number of shares owned by the user for a given stock.
     */
    @PostMapping("/stockshares")
    public ResponseEntity<String> getStockShares(@RequestAttribute("uid") String uid, @RequestBody Map<String, String> body
        ) throws ExecutionException, InterruptedException {

        String stockSymbol = body.get("stockSymbol");

        ResponseEntity<JsonObject> response = serverService.getStockShares(uid, stockSymbol);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody().toString());
    }
    /**
     * Executes a stock purchase for the user.
     */
    @PostMapping("/buy")
    public ResponseEntity<String> buy( @RequestAttribute("uid") String uid, @RequestBody Map<String, String> body
    ) throws ExecutionException, InterruptedException {

        String stockSymbol = body.get("stockSymbol");
        int quantity = Integer.parseInt(body.get("quantity"));
        ResponseEntity<String> response = serverService.buyStock(uid, stockSymbol, quantity);

        return response;
    }
    /**
     * Executes a stock sale for the user.
     */
    @PostMapping("/sell")
    public ResponseEntity<String> sell( @RequestAttribute("uid") String uid, @RequestBody Map<String, String> body
    ) throws ExecutionException, InterruptedException {


        String stockSymbol = body.get("stockSymbol");
        int quantity = Integer.parseInt(body.get("quantity"));
        ResponseEntity<String> response = serverService.sellStock(uid, stockSymbol, quantity);

        return response;
    }
    /**
     * Returns all stock investments the user holds.
     */
    @PostMapping("/investments")
    public ResponseEntity<String> getInvestments(@RequestAttribute("uid") String uid)
            throws ExecutionException, InterruptedException {

        ResponseEntity<String > response = serverService.getInvestments(uid);

        return response;
    }
    /**
     * Returns portfolio performance change data (e.g., % gain/loss).
     */
    @PostMapping("/portfolio/change")
    public ResponseEntity<Map<String, Double>> getPortfolioChange(@RequestAttribute("uid") String uid)
            throws ExecutionException, InterruptedException {

        return serverService.getPortfolioChange(uid);
    }
    /**
     * Returns the user's full transaction history.
     */
    @PostMapping("/history")
    public ResponseEntity<String> getTransactionHistory(@RequestAttribute("uid") String uid) {
        ResponseEntity<String> response = serverService.getTransactionHistory(uid);
        return response;
    }
    /**
     * Returns the user's current watchlist.
     */
    @PostMapping("/watchlist/get")
    public ResponseEntity<String> getWatchlist(@RequestAttribute("uid") String uid) {
        ResponseEntity<JsonArray> response = serverService.getWatchlist(uid);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody().toString());
    }
    /**
     * Checks if a given stock is in the user's watchlist.
     */
    @PostMapping("/watchlist/in")
    public ResponseEntity<String> inWatchlist(@RequestAttribute("uid") String uid, @RequestBody Map<String, String> body) {

        String stockSymbol = body.get("stockSymbol");
        ResponseEntity<Boolean> response = serverService.inWatchlist(uid, stockSymbol);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody().toString());
    }
    /**
     * Adds a stock to the user's watchlist.
     */
    @PostMapping("/watchlist/add")
    public ResponseEntity<String> addToWatchlist(@RequestAttribute("uid") String uid, @RequestBody Map<String, String> body) {
        String stockSymbol = body.get("stockSymbol");
        return serverService.addToWatchlist(uid, stockSymbol);
    }
    /**
     * Removes a stock from the user's watchlist.
     */
    @PostMapping("/watchlist/remove")
    public ResponseEntity<String> removeFromWatchlist(@RequestAttribute("uid") String uid, @RequestBody Map<String, String> body) {
        String stockSymbol = body.get("stockSymbol");
        return serverService.removeFromWatchlist(uid, stockSymbol);
    }
    /**
     * Returns a breakdown of user's portfolio by industry sector.
     */
    @PostMapping("/industries")
    public ResponseEntity<String> getIndustryBreakdown(@RequestAttribute("uid") String uid) {
        System.out.println("in industries controller method:" + uid);
        ResponseEntity<JsonObject> response = serverService.getIndustryBreakdown(uid);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody().toString());
    }
    /**
     * Resets the user's account data
     */
    @PostMapping("/reset")
    public ResponseEntity<String> resetAccount(@RequestAttribute("uid") String uid) {
        return serverService.resetAccount(uid);
    }
}