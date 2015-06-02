package net.cs50.finance.models;

import net.cs50.finance.models.dao.StockHoldingDao;
import net.cs50.finance.models.dao.StockTransactionDao;
import net.cs50.finance.models.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by cbay on 5/10/15.
 */

/**
 * Represents a user's ownership stake in a particular stock
 */
@Entity
@Table(name = "stock_holdings")
public class StockHolding extends AbstractEntity {

    private String symbol;
    private int sharesOwned;
    private int ownerId;

    /**
     * The history of past transactions in which the owner bought or sold shares from this stock holding
     */
    private List<StockTransaction> transactions;

    private StockHolding() {}

    private StockHolding(String symbol, int ownerId) {
        // TODO - make sure symbol is always upper or lowercase (your choice)
        this.symbol = symbol.toUpperCase();
        this.sharesOwned = 0;
        this.ownerId = ownerId;
        transactions = new ArrayList<StockTransaction>();
    }

    @NotNull
    @Column(name = "owner_id", nullable = false)
    public int getOwnerId(){
        return ownerId;
    }

    protected void setOwnerId(int ownerId){
        this.ownerId = ownerId;
    }

    @NotNull
    @Column(name = "symbol", nullable = false)
    public String getSymbol() {
        return symbol;
    }

    protected void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @NotNull
    @Column(name = "shares_owned", nullable = false)
    public int getSharesOwned() {
        return sharesOwned;
    }

    protected void setSharesOwned(int sharesOwned) {
        this.sharesOwned = sharesOwned;
    }

    @OneToMany(mappedBy = "stockHolding", cascade = CascadeType.PERSIST)
    public List<StockTransaction> getTransactions() {
        return transactions;
    }

    protected void setTransactions(List<StockTransaction> transactions) {
        this.transactions = transactions;
    }

    /**
     * Instance method for buying shares of a holding
     *
     * @param numberOfShares
     * @throws IllegalArgumentException if numberOfShares < 0
     * @throws StockLookupException     if unable to lookup stock info
     */
    private void buyShares(int numberOfShares) throws StockLookupException {

        if (numberOfShares < 0) {
            throw new IllegalArgumentException("Can not purchase a negative number of shares.");
        }

        setSharesOwned(sharesOwned + numberOfShares);

        StockTransaction transaction = new StockTransaction(this, numberOfShares, StockTransaction.TransactionType.BUY);
        this.transactions.add(transaction);

    }

    /**
     * Instance method for selling shares of a holding
     *
     * @param numberOfShares
     * @throws IllegalArgumentException if numberOfShares greater than shares owned
     * @throws StockLookupException     if unable to lookup stock info
     */
    private void sellShares(int numberOfShares) throws StockLookupException {

        if (numberOfShares > sharesOwned) {
            throw new IllegalArgumentException("Number to sell exceeds shares owned for stock" + symbol);
        }

        setSharesOwned(sharesOwned - numberOfShares);

        StockTransaction transaction = new StockTransaction(this, numberOfShares, StockTransaction.TransactionType.SELL);
        this.transactions.add(transaction);
    }

    /**
     * Static method for buying shares of a StockHolding. Creates a new holding if the user did not already have one,
     * otherwise simply updates sharesOwned on the existing holding
     *
     * @param user              user to buy the stock
     * @param symbol            symbol of the stock to buy
     * @param numberOfShares    number of shares to buy
     * @return                  the holding for the user and symbol
     * @throws IllegalArgumentException
     */
    public static StockHolding buyShares(User user, String symbol, int numberOfShares) throws StockLookupException, IllegalArgumentException {

        symbol = symbol.toUpperCase();

        // make sure user can afford to buy this many shares
        Stock stock = Stock.lookupStock(symbol);
        if (user.getCash() < stock.getPrice() * numberOfShares) {
            throw new IllegalArgumentException("User does not have enough cash to buy this much stock");
        }

        // Get portfolio
        Map<String, StockHolding> userPortfolio = user.getPortfolio();

        // Create new holding, if user has never owned the stock before
        if (!userPortfolio.containsKey(symbol)) {
            StockHolding newHolding = new StockHolding(symbol, user.getUid());
            user.addHolding(newHolding);
        }

        // Conduct buy
        StockHolding holding = userPortfolio.get(symbol);
        holding.buyShares(numberOfShares);

        // update user cash
        user.setCash(user.getCash() - stock.getPrice() * numberOfShares);

        return holding;
    }

    /**
     * Static method for selling shares of a StockHolding.
     *
     * @param user              owner of the holding
     * @param symbol            symbol of the holding to sell
     * @param numberOfShares    number of shares to sell
     * @return                  the given holding for symbol and user, or null if user has never owned any of symbol's stock
     * @throws IllegalArgumentException
     */
    public static StockHolding sellShares(User user, String symbol, int numberOfShares) throws StockLookupException, IllegalArgumentException {

        symbol = symbol.toUpperCase();

        // Get existing holding
        Map<String, StockHolding> userPortfolio = user.getPortfolio();

        // make sure usr actually owns stock in this company
        if (!userPortfolio.containsKey(symbol)) {
            throw new IllegalArgumentException("You don't own any shares of this stock");
        }

        // Conduct sale
        StockHolding holding = userPortfolio.get(symbol);
        holding.sellShares(numberOfShares);

        // update user cash
        Stock currentStock = Stock.lookupStock(symbol);
        user.setCash(user.getCash() + currentStock.getPrice() * numberOfShares);

        return holding;
    }
}
