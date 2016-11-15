package org.launchcode.stocks.controllers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.launchcode.stocks.models.Stock;
import org.launchcode.stocks.models.StockHolding;
import org.launchcode.stocks.models.StockLookupException;
import org.launchcode.stocks.models.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Chris Bay on 5/17/15.
 */
@Controller
public class PortfolioController extends AbstractController {

	@RequestMapping(value = "/portfolio")
	public String portfolio(HttpServletRequest request, Model model) throws StockLookupException {

		// TODO - Implement portfolio display
		User user = getUserFromSession(request);
		Map<String, StockHolding> portfolio = user.getPortfolio();
		Map<String, List<String>> stocks = holdingsMap(portfolio);

		/*
		 * Display the user's stocks in a table. You'll find some code already
		 * in place in PortfolioController.java and the portfolio.html template.
		 * You should display the following fields for each stock in the
		 * template: display name (use Stock.toString()), number of shares
		 * owned, current price, and total value of shares owned. Format the
		 * currency values appropriately, with 2 decimal places. You may find
		 * the th:each Thymleaf tag useful here.
		 */

		model.addAttribute("stocks", stocks);
		model.addAttribute("title", "Portfolio");
		model.addAttribute("portfolioNavClass", "active");

		return "portfolio";
	}

	public static Map<String, List<String>> holdingsMap(Map<String, StockHolding> portfolio) {
		Map<String, List<String>> stocks = new HashMap<>();

		try {
			for (Map.Entry<String, StockHolding> entry : portfolio.entrySet()) {

				String symbol = entry.getKey();
				Stock s = Stock.lookupStock(symbol);
				Integer shares = entry.getValue().getSharesOwned();

				if (shares != 0) {

					String name = s.toString();
					String numShares = String.valueOf(shares);
					String price = String.format("%.2f", s.getPrice());
					String value = String.format("%.2f", shares * s.getPrice());

					stocks.put(symbol, Arrays.asList(name, numShares, price, value));
				}
			}

		} catch (StockLookupException e) {
			e.printStackTrace();
		}

		return stocks;
	}

}
