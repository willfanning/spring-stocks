package org.launchcode.stocks.controllers;

import net.cs50.finance.models.Stock;
import net.cs50.finance.models.StockHolding;
import net.cs50.finance.models.StockLookupException;
import net.cs50.finance.models.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Chris Bay on 5/17/15.
 */
@Controller
public class PortfolioController extends AbstractController {

    @RequestMapping(value = "/portfolio")
    public String portfolio(HttpServletRequest request, Model model){

        User user = this.getUserFromSession(request);

        // assemble portfolio data to pass to template

        ArrayList<HashMap<String, String>> portfolioData = new ArrayList<HashMap<String, String>>();

        Collection<StockHolding> holdings = user.getPortfolio().values();

        // for each holding the user has, add an entry to portfolio data
        for (StockHolding holding : holdings) {

            // assemble a hashmap with the data for this holding

            HashMap<String, String> holdingData = new HashMap<String, String>();

            // add shares to the holding data
            holdingData.put("shares", String.valueOf(holding.getSharesOwned()));

            // lookup current stock info
            Stock currentStock = null;
            try {
                currentStock = Stock.lookupStock(holding.getSymbol());
            } catch (StockLookupException e) {
                e.printStackTrace();
                return this.displayError("Unable to display portfolio", model);
            }

            // add description to the holding data
            holdingData.put("description", currentStock.toString());

            // add price to the holding data
            String priceDisplay = "$" + String.format("%.2f", currentStock.getPrice());
            holdingData.put("price", priceDisplay);

            // add total value to the holding data
            float totalValue = currentStock.getPrice() * holding.getSharesOwned();
            String totalValueDisplay = "$" + String.format("%.2f", totalValue);
            holdingData.put("total_value", totalValueDisplay);

            portfolioData.add(holdingData);
        }

        model.addAttribute("portfolioData", portfolioData);
        model.addAttribute("cash", "$" + String.format("%.2f", user.getCash()));
        model.addAttribute("title", "Portfolio");
        model.addAttribute("portfolioNavClass", "active");

        return "portfolio";
    }

}
