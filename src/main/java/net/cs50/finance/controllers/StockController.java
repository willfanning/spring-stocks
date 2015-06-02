package net.cs50.finance.controllers;

import net.cs50.finance.models.*;
import net.cs50.finance.models.dao.StockHoldingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Chris Bay on 5/17/15.
 */
@Controller
public class StockController extends AbstractFinanceController {

    @Autowired
    StockHoldingDao stockHoldingDao;

    @RequestMapping(value = "/quote", method = RequestMethod.GET)
    public String quoteForm(Model model) {

        // pass data to template
        model.addAttribute("title", "Quote");
        model.addAttribute("quoteNavClass", "active");
        return "quote_form";
    }

    @RequestMapping(value = "/quote", method = RequestMethod.POST)
    public String quote(String symbol, Model model) {

        // Implement quote lookup

        // attempt to get the current stock instance
        Stock stock = null;
        try {
            stock = Stock.lookupStock(symbol);
        } catch (StockLookupException e) {
            e.printStackTrace();
            return this.displayError("Something went wrong. Make sure you provide a valid company symbol", model);
        }

        // pass data to template
        model.addAttribute("stock_desc", stock.toString());
        model.addAttribute("stock_price", "$" + stock.getPrice());
        model.addAttribute("title", "Quote");
        model.addAttribute("quoteNavClass", "active");

        return "quote_display";
    }

    @RequestMapping(value = "/buy", method = RequestMethod.GET)
    public String buyForm(Model model) {

        model.addAttribute("title", "Buy");
        model.addAttribute("action", "/buy");
        model.addAttribute("buyNavClass", "active");
        return "transaction_form";
    }

    @RequestMapping(value = "/buy", method = RequestMethod.POST)
    public String buy(String symbol, int numberOfShares, HttpServletRequest request, Model model) {

        // Implement buy action

        // get user
        User user = this.getUserFromSession(request);

        // attempt to conduct buy
        StockHolding holding = null;
        try {
            holding = StockHolding.buyShares(user, symbol, numberOfShares);
        } catch (StockLookupException e) {
            e.printStackTrace();
            return this.displayError("Something went wrong. Make sure you provide a valid company symbol", model);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return this.displayError("You don't have enough cash to buy " + numberOfShares + " shares of " + symbol, model);
        }

        // persist changes
        this.stockHoldingDao.save(holding);
        this.userDao.save(user);

        // pass data to template
        model.addAttribute("confirmMessage", "You bought " + numberOfShares + " shares of " + symbol);
        model.addAttribute("title", "Buy");
        model.addAttribute("action", "/buy");
        model.addAttribute("buyNavClass", "active");

        return "transaction_confirm";
    }

    @RequestMapping(value = "/sell", method = RequestMethod.GET)
    public String sellForm(Model model) {
        model.addAttribute("title", "Sell");
        model.addAttribute("action", "/sell");
        model.addAttribute("sellNavClass", "active");
        return "transaction_form";
    }

    @RequestMapping(value = "/sell", method = RequestMethod.POST)
    public String sell(String symbol, int numberOfShares, HttpServletRequest request, Model model) {

        // attempt to sell shares of the stock selected by the user
        User user = this.getUserFromSession(request);

        // attempt to conduct sale
        StockHolding holding = null;
        try {
            holding = StockHolding.sellShares(user, symbol, numberOfShares);
        } catch (StockLookupException e) {
            e.printStackTrace();
            return this.displayError("Unable to sell", model);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return this.displayError("Can't sell " + numberOfShares + " shares of " + symbol + " because you don't own that many shares.", model);
        }

        // persist changes
        this.stockHoldingDao.save(holding);
        this.userDao.save(user);

        // pass data to template
        model.addAttribute("confirmMessage", "You sold " + numberOfShares + " shares of " + symbol);
        model.addAttribute("title", "Sell");
        model.addAttribute("action", "/sell");
        model.addAttribute("sellNavClass", "active");

        return "transaction_confirm";
    }

}
